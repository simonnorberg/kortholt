package net.simno.kortholt

import android.app.Application
import kotlinx.coroutines.Dispatchers

class SampleApp : Application(), Kortholt.Player.Factory {
    override fun newPlayer() = Kortholt.Player.Builder(this)
        .dispatcher(Dispatchers.IO)
        .build()
}
