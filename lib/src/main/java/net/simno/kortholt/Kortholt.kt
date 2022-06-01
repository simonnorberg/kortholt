package net.simno.kortholt

import android.content.Context
import android.media.AudioManager
import android.media.AudioManager.PROPERTY_OUTPUT_FRAMES_PER_BUFFER
import android.media.AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE
import androidx.core.content.getSystemService
import java.util.concurrent.atomic.AtomicLong

object Kortholt {

    private val kortholtHandle = AtomicLong(0L)

    init {
        System.loadLibrary("kortholt")
    }

    fun create(context: Context) {
        destroy()
        setDefaultStreamValues(context)
        kortholtHandle.set(nativeCreateKortholt(getExclusiveCores()))
    }

    fun destroy() {
        kortholtHandle.getAndSet(0L).takeIf { it != 0L }?.let { nativeDeleteKortholt(it) }
    }

    private fun setDefaultStreamValues(context: Context) {
        context.getSystemService<AudioManager>()?.let { am ->
            runCatching {
                val sampleRate = am.getProperty(PROPERTY_OUTPUT_SAMPLE_RATE)?.toInt()
                val framesPerBurst = am.getProperty(PROPERTY_OUTPUT_FRAMES_PER_BUFFER)?.toInt()
                if (sampleRate != null && framesPerBurst != null) {
                    nativeSetDefaultStreamValues(sampleRate, framesPerBurst)
                }
            }
        }
    }

    private fun getExclusiveCores(): IntArray {
        return runCatching { android.os.Process.getExclusiveCores() }.getOrDefault(intArrayOf())
    }

    private external fun nativeCreateKortholt(cpuIds: IntArray): Long
    private external fun nativeDeleteKortholt(kortholtHandle: Long)
    private external fun nativeSetDefaultStreamValues(sampleRate: Int, framesPerBurst: Int)
}
