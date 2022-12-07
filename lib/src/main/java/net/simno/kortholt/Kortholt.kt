package net.simno.kortholt

import android.content.Context
import android.media.AudioManager
import android.media.AudioManager.PROPERTY_OUTPUT_FRAMES_PER_BUFFER
import android.media.AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE
import androidx.core.content.getSystemService
import java.io.File
import java.util.concurrent.atomic.AtomicLong
import kotlin.time.Duration

object Kortholt {

    private val kortholtHandle = AtomicLong(0L)

    init {
        System.loadLibrary("kortholt")
    }

    fun create(context: Context) {
        create(context, stream = true)
    }

    private fun create(context: Context, stream: Boolean) {
        destroy()
        setDefaultStreamValues(context)
        kortholtHandle.set(nativeCreateKortholt(getExclusiveCores(), stream))
    }

    fun destroy() {
        kortholtHandle.getAndSet(0L).takeIf { it != 0L }?.let { nativeDeleteKortholt(it) }
    }

    @ExperimentalWaveFile
    fun saveWaveFile(
        context: Context,
        outputFile: File,
        duration: Duration,
        startBang: String = "",
        stopBang: String = ""
    ): Int = runCatching {
        create(context, stream = false)
        nativeSaveWaveFile(
            kortholtHandle = kortholtHandle.get(),
            fileName = outputFile.absolutePath,
            duration = duration.inWholeMilliseconds,
            startBang = startBang,
            stopBang = stopBang
        )
    }.getOrDefault(0)

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

    private external fun nativeCreateKortholt(cpuIds: IntArray, stream: Boolean): Long
    private external fun nativeDeleteKortholt(kortholtHandle: Long)
    private external fun nativeSetDefaultStreamValues(sampleRate: Int, framesPerBurst: Int)
    private external fun nativeSaveWaveFile(
        kortholtHandle: Long,
        fileName: String,
        duration: Long,
        startBang: String,
        stopBang: String
    ): Int
}
