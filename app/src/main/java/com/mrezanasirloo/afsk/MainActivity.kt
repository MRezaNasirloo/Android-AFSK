package com.mrezanasirloo.afsk

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val stream = resources.openRawResource(R.raw.file_1)
        val header = WaveHeader.read(stream)
        val message = SignalDecoder(header, 3200).decode(stream.apply { reset() })

        textView.movementMethod = ScrollingMovementMethod()
        textView.text = message.read()
    }
}
