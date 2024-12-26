package net.simno.kortholt.sample

import android.app.Application
import kotlinx.coroutines.Dispatchers
import net.simno.kortholt.Kortholt

class SampleApp :
    Application(),
    Kortholt.Player.Factory {
    override fun newPlayer() = Kortholt.Player.Builder(this)
        .dispatcher(Dispatchers.IO)
        .build()
}
