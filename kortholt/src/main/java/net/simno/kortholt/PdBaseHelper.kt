package net.simno.kortholt

import android.content.Context
import androidx.annotation.RawRes
import org.puredata.core.PdBase
import org.puredata.core.PdBaseLoader
import org.puredata.core.utils.IoUtils
import java.io.File
import java.io.IOException

object PdBaseHelper {

    private var patchHandle = 0

    init {
        PdBaseLoader.loaderHandler = object : PdBaseLoader() {
            override fun load() {
                System.loadLibrary("pd")
                System.loadLibrary("pdnative")
            }
        }
    }

    fun openPatch(
        context: Context,
        @RawRes patchRes: Int,
        patchName: String,
        extractZip: Boolean = false
    ) {
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
        } finally {
            try {
                patchFile?.delete()
            } catch (ignored: Exception) {
            }
        }
    }

    fun closePatch() {
        PdBase.closePatch(patchHandle)
    }
}
