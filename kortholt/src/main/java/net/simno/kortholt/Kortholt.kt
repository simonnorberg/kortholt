package net.simno.kortholt

import android.content.Context
import android.media.AudioManager
import android.media.AudioManager.PROPERTY_OUTPUT_FRAMES_PER_BUFFER
import android.media.AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE
import androidx.annotation.RawRes
import androidx.core.content.getSystemService
import org.puredata.core.PdBase
import org.puredata.core.PdBaseLoader
import org.puredata.core.utils.IoUtils
import java.io.File
import java.io.IOException

object Kortholt {
    private var engineHandle = 0L
    private var patchHandle = 0

    init {
        PdBaseLoader.loaderHandler = object : PdBaseLoader() {
            override fun load() {
                System.loadLibrary("pd")
                System.loadLibrary("pdnative")
            }
        }
        System.loadLibrary("kortholt")
    }

    fun create(context: Context): Boolean {
        if (engineHandle == 0L) {
            setDefaultStreamValues(context)
            engineHandle = nativeCreateEngine()
        }
        return engineHandle != 0L
    }

    private fun setDefaultStreamValues(context: Context) {
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
    }

    fun open(
        context: Context,
        @RawRes patchRes: Int,
        patchName: String,
        extractZip: Boolean = false
    ): Boolean {
        PdBase.addToSearchPath(context.applicationInfo.nativeLibraryDir)
        val dir = context.cacheDir
        var patchFile: File? = null
        try {
            context.resources.openRawResource(patchRes).use { input ->
                patchFile = if (extractZip) {
                    IoUtils.extractZipResource(input, dir, true)
                    File(dir, patchName)
                } else {
                    IoUtils.extractResource(input, patchName, dir)
                }
            }
            patchHandle = PdBase.openPatch(patchFile)
        } catch (ignored: IOException) {
            return false
        } finally {
            try {
                patchFile?.delete()
            } catch (ignored: Exception) {
            }
        }
        return true
    }

    fun destroy() {
        PdBase.closePatch(patchHandle)
        if (engineHandle != 0L) {
            nativeDeleteEngine(engineHandle)
        }
        engineHandle = 0
    }

    private external fun nativeCreateEngine(): Long
    private external fun nativeDeleteEngine(engineHandle: Long)
    private external fun nativeSetDefaultStreamValues(sampleRate: Int, framesPerBurst: Int)
}
