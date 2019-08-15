package net.simno.kortholt

import android.content.Context
import android.media.AudioManager
import android.media.AudioManager.PROPERTY_OUTPUT_FRAMES_PER_BUFFER
import android.media.AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE
import androidx.core.content.getSystemService

object Kortholt {

    private var kortholtHandle = 0L

    init {
        System.loadLibrary("kortholt")
    }

    fun create(context: Context) {
        context.getSystemService<AudioManager>()?.let { am ->
            try {
                val sampleRate = am.getProperty(PROPERTY_OUTPUT_SAMPLE_RATE)?.toInt()
                val framesPerBurst = am.getProperty(PROPERTY_OUTPUT_FRAMES_PER_BUFFER)?.toInt()
                if (sampleRate != null && framesPerBurst != null) {
                    nativeSetDefaultStreamValues(sampleRate, framesPerBurst)
                }
            } catch (ignored: NumberFormatException) {
            }
        }
        kortholtHandle = nativeCreateKortholt()
    }

    fun destroy() {
        if (kortholtHandle != 0L) {
            nativeDeleteKortholt(kortholtHandle)
            kortholtHandle = 0L
        }
    }

    private external fun nativeCreateKortholt(): Long
    private external fun nativeDeleteKortholt(kortholtHandle: Long)
    private external fun nativeSetDefaultStreamValues(sampleRate: Int, framesPerBurst: Int)
}
