package net.simno.kortholt

import android.content.Context

inline val Context.kortholt: Kortholt.Player
    get() = Kortholt.player(this)
