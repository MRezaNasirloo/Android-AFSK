package com.mrezanasirloo.afsk

import org.junit.Assert
import org.junit.Test
import java.io.File
import java.io.FileInputStream

class SignalDecoderTestTest {


    @Test
    fun should_parse_header_file() {
        val file = File("src/test/resources/file_1.wav")
        val header = WaveHeader.read(FileInputStream(file))
        Assert.assertEquals(44100, header.sampleRate)
        Assert.assertEquals(2.toShort(), header.numChannels)
        Assert.assertEquals(1.toShort(), header.audioFormat)
        Assert.assertEquals("RIFF", header.chunkID)
    }

    @Test
    fun should_contain_30_messages() {
        val file = File("src/test/resources/file_1.wav")
        val stream = FileInputStream(file)
        val header = WaveHeader.read(stream)
        val encodedMessage = SignalDecoder(header, 3200)
            .decode(FileInputStream(file))

        Assert.assertTrue(encodedMessage.isValid)
        //Since the checksum algorithm has not been specified
        // in the question this test fails

    }
}
