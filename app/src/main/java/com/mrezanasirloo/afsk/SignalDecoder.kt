package com.mrezanasirloo.afsk

import java.io.InputStream
import java.util.*

class SignalDecoder(
    private val header: WaveHeader,
    frameLength: Int
) {

    private val zeroIdentifier = header.sampleRate / frameLength * 2
    private val oneIdentifier = header.sampleRate / frameLength
    private val message = EncodedMessage()
    private var lastSignal = STATE.Silent
    private var frameCounter = 0
    private var bitCounter = 0
    private val bits = BitSet()

    fun decode(inputStream: InputStream): EncodedMessage {
        inputStream.use { stream ->
            stream.skip(WaveHeader.HEADER_SIZE)
            val bufferSize = header.subChunk2Size
            val data = ByteArray(bufferSize)
            val chunk = ShortArray(bufferSize / 2)
            var bytes: Int
            while (stream.read(data).also { bytes = it } > 0) {
                for (i in 0 until bytes step 2) {
                    chunk[i / 2] = ((data[i + 1].toInt() shl 8) or (data[i].toInt() and 0xFF)).toShort()
                }
                val mono = toMonoChannel(chunk, bytes / 2)
                processSignal(mono)
            }
            return message
        }
    }

    private fun toMonoChannel(data: ShortArray, count: Int): ShortArray {
        val mono = ShortArray(data.size / 2)
        var i = 0
        while (i < count - 1) {
            val mixed = (data[i] + data[i + 1]) / 2
            mono[i / 2] = mixed.toShort()
            i += 2
        }
        return mono
    }

    private fun processSignal(signals: ShortArray) {
        for (signal in signals) {
            val currentSignal = when {
                signal > 0 -> STATE.High
                signal < 0 -> STATE.Low
                else -> STATE.Silent
            }
            if (lastSignal == STATE.Silent) {
                lastSignal = currentSignal
                continue
            }
            if (currentSignal == lastSignal) {
                frameCounter++
            } else {
                if (frameCounter >= zeroIdentifier) {
                    bits.set(bitCounter++, false)
                    frameCounter = 0
                } else if (frameCounter >= oneIdentifier) {
                    bits.set(bitCounter++, true)
                    frameCounter = 0
                }
            }
            lastSignal = currentSignal

            if (bitCounter > 10 && (bitCounter - 1) % 10 == 0) {
                message.add(bits)
                bitCounter = 0
                bits.clear()
            }
        }
    }

    private enum class STATE {
        High,
        Low,
        Silent
    }
}