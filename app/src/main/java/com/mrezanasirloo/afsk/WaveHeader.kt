package com.mrezanasirloo.afsk

import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.StandardCharsets.US_ASCII

/**
 * More info about [WaveFormat][http://soundfile.sapp.org/doc/WaveFormat/]
 */
data class WaveHeader(
    val chunkID: String,
    val chunkSize: Int,
    val format: String,
    val subChunk1ID: String,
    val subChunk1Size: Int,
    val audioFormat: Short,
    val numChannels: Short,
    val sampleRate: Int,
    val byteRate: Int,
    val blockAlign: Short,
    val bitsPerSample: Short,
    val subChunk2ID: String,
    val subChunk2Size: Int
) {
    companion object {
        const val HEADER_SIZE = 44L

        fun read(stream: InputStream): WaveHeader {
            val buffer = ByteBuffer.allocate(HEADER_SIZE.toInt())
            buffer.order(ByteOrder.LITTLE_ENDIAN)
            stream.read(buffer.array(), buffer.arrayOffset(), buffer.capacity())
            val bytes = ByteArray(4)
            return WaveHeader(
                chunkID = buffer.string(bytes),
                chunkSize = buffer.int,
                format = buffer.string(bytes),
                subChunk1ID = buffer.string(bytes),
                subChunk1Size = buffer.int,
                audioFormat = buffer.short,
                numChannels = buffer.short,
                sampleRate = buffer.int,
                byteRate = buffer.int,
                blockAlign = buffer.short,
                bitsPerSample = buffer.short,
                subChunk2ID = buffer.string(bytes),
                subChunk2Size = buffer.int
            )
        }

        private fun ByteBuffer.string(bytes: ByteArray): String {
            get(bytes)
            return bytes.toString(US_ASCII)
        }
    }
}