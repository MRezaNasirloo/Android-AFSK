package com.mrezanasirloo.afsk

import java.util.*
import kotlin.collections.ArrayList

data class Message(private val _byte: BitSet) {

    val byte: Byte = when {
        _byte.cardinality() == 11 || _byte.cardinality() == 0 -> 0x00
        !_byte[0] && _byte[9] && _byte[10] -> {
            _byte.get(1, 8).toByteArray().takeIf { it.isNotEmpty() }?.get(0) ?: 0x00
        }
        else -> throw IllegalStateException("invalid Byte : $_byte")

    }

    val char: Char
        get() = byte.toChar()
}

class ToneData {
    val data: ArrayList<Message> = ArrayList(30) // 30 messages
    lateinit var checksum: Message

    val filled: Boolean
        get() = data.size == 30 && this::checksum.isInitialized

    fun add(message: Message): ToneData {
        if (data.size < 30) {
            data.add(message)
            return this
        }
        if (this::checksum.isInitialized.not()) {
            checksum = message
        } else {
            throw IllegalStateException("Checksum already set")
        }
        return this
    }
}

class EncodedMessage {
    private val id: Array<Message?> = Array(2) { null } // 2 messages
    private val data: ArrayList<ToneData> = ArrayList(64) // (30 + 1) x 64 = 1984  messages
    private var endId: Message? = null // 1 messages

    private var state = State.LEAD

    fun add(bits: BitSet) {
        if (bits.isEmpty || state == State.END) return
        val message = Message(bits.clone() as BitSet)

        if (state == State.DATA) {
            add(message)
        }

        if (state == State.DATA && message.byte.toInt() == 0x00) {
            this.endId = message
            state = State.END
        }

        val id0 = id[0]
        if (id0 != null && id0.byte.toInt() == 0x42 && message.byte.toInt() == 0x03) {
            state = State.DATA
        } else {
            id[0] = message
        }
    }

    private fun add(message: Message) {
        if (data.size < 64) {
            data.lastOrNull()?.also {
                if (it.filled.not()) {
                    it.add(message)
                    return
                }
            }
            data.add(ToneData().add(message))
            return
        }
        if (data.size == 64 && data.last().filled.not()) {
            data.last().add(message)
            return
        }
    }

    fun read(): String {
        val builder = StringBuilder(data.size * 30)
        data.forEach { tone ->
            tone.data.forEach { builder.append(it.char) }
        }
        return builder.toString()
    }

    private enum class State {
        LEAD,
        DATA,
        END
    }
}