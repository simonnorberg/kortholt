package net.simno.kortholt

import android.content.Context
import androidx.annotation.RawRes
import java.io.File
import kotlin.time.Duration
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

object Kortholt {

    private var factory: Player.Factory? = null
    private var player: Player? = null

    @JvmStatic
    fun player(context: Context): Player {
        return player ?: newPlayer(context)
    }

    @JvmStatic
    @Synchronized
    fun setPlayer(player: Player) {
        this.factory = null
        this.player = player
    }

    @JvmStatic
    @Synchronized
    fun setPlayer(factory: Player.Factory) {
        this.factory = factory
        this.player = null
    }

    @JvmStatic
    @Synchronized
    fun reset() {
        this.factory = null
        this.player = null
    }

    @Synchronized
    private fun newPlayer(context: Context): Player {
        player?.let { return it }

        val newPlayer = factory?.newPlayer()
            ?: (context.applicationContext as? Player.Factory)?.newPlayer()
            ?: Player.Builder(context).build()
        factory = null
        player = newPlayer

        return newPlayer
    }

    interface Player {
        suspend fun openPatch(
            @RawRes patchRes: Int,
            patchName: String,
            extractZip: Boolean = false
        ): Boolean

        suspend fun closePatch(): Boolean
        suspend fun startStream(): Boolean
        suspend fun stopStream(): Boolean
        fun sendBang(receiver: String)
        fun sendFloat(receiver: String, x: Float)
        fun sendList(receiver: String, vararg args: Any)

        @ExperimentalWaveFile
        suspend fun saveWaveFile(
            outputFile: File,
            duration: Duration,
            startBang: String = "",
            stopBang: String = ""
        ): Int

        fun interface Factory {
            fun newPlayer(): Player
        }

        class Builder(context: Context) {
            private val applicationContext: Context = context.applicationContext
            private var dispatcher: CoroutineDispatcher = Dispatchers.IO

            fun dispatcher(dispatcher: CoroutineDispatcher): Builder = apply {
                this.dispatcher = dispatcher
            }

            fun build(): Player = KortholtPlayer(
                context = applicationContext,
                dispatcher = dispatcher
            )
        }
    }
}
