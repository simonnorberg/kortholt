package net.simno.kortholt

import android.content.Context
import android.media.AudioManager
import android.os.Process
import androidx.annotation.RawRes
import androidx.core.content.getSystemService
import com.getkeepsafe.relinker.ReLinker
import java.io.File
import java.util.concurrent.atomic.AtomicLong
import kotlin.time.Duration
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.puredata.core.PdBase
import org.puredata.core.PdBaseLoader
import org.puredata.core.utils.IoUtils

internal class KortholtPlayer(
    private val context: Context,
    private val dispatcher: CoroutineDispatcher
) : Kortholt.Player {

    private val patchHandle = AtomicLong(NOT_SET)
    private val kortholtHandle = AtomicLong(NOT_SET)

    init {
        PdBaseLoader.loaderHandler = object : PdBaseLoader() {
            override fun load() {
                ReLinker.loadLibrary(context, "pd", VERSION)
                ReLinker.loadLibrary(context, "pdnative", VERSION)
                ReLinker.loadLibrary(context, "kortholt", VERSION)
            }
        }
    }

    override suspend fun openPatch(
        @RawRes patchRes: Int,
        patchName: String,
        extractZip: Boolean
    ) = withContext(dispatcher) {
        runCatching {
            PdBase.addToSearchPath(context.applicationInfo.nativeLibraryDir)
            val dir = context.cacheDir
            var patchFile: File? = null
            runCatching {
                context.resources.openRawResource(patchRes).use { input ->
                    patchFile = if (extractZip) {
                        IoUtils.extractZipResource(input, dir, true)
                        File(dir, patchName)
                    } else {
                        IoUtils.extractResource(input, patchName, dir)
                    }
                }
                patchHandle.set(PdBase.openPatch(patchFile).toLong())
            }
            patchFile?.delete()
        }.isSuccess
    }

    override suspend fun closePatch() = withContext(dispatcher) {
        runCatching {
            patchHandle.getAndSet(NOT_SET).takeIf { it != NOT_SET }?.let { PdBase.closePatch(it.toInt()) }
        }.isSuccess
    }

    override suspend fun startStream() = create(stream = true)

    private suspend fun create(stream: Boolean) = withContext(dispatcher) {
        runCatching {
            stopStream()
            setDefaultStreamValues()
            kortholtHandle.set(nativeCreateKortholt(getExclusiveCores(), stream))
        }.isSuccess
    }

    override suspend fun stopStream() = withContext(dispatcher) {
        runCatching {
            kortholtHandle.getAndSet(NOT_SET).takeIf { it != NOT_SET }?.let { nativeDeleteKortholt(it) }
        }.isSuccess
    }

    @ExperimentalWaveFile
    override suspend fun saveWaveFile(
        outputFile: File,
        duration: Duration,
        startBang: String,
        stopBang: String
    ) = withContext(dispatcher) {
        runCatching {
            create(stream = false)
            nativeSaveWaveFile(
                kortholtHandle = kortholtHandle.get(),
                fileName = outputFile.absolutePath,
                duration = duration.inWholeMilliseconds,
                startBang = startBang,
                stopBang = stopBang
            )
        }.getOrDefault(0)
    }

    private fun setDefaultStreamValues() {
        runCatching {
            context.getSystemService<AudioManager>()?.let { am ->
                val sampleRate = am.getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE)?.toInt()
                val framesPerBurst = am.getProperty(AudioManager.PROPERTY_OUTPUT_FRAMES_PER_BUFFER)?.toInt()
                if (sampleRate != null && framesPerBurst != null) {
                    nativeSetDefaultStreamValues(sampleRate, framesPerBurst)
                }
            }
        }
    }

    private fun getExclusiveCores() = runCatching { Process.getExclusiveCores() }.getOrDefault(intArrayOf())

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

    companion object {
        private const val NOT_SET = -1L
        private const val VERSION = "2.0.0"
    }
}
