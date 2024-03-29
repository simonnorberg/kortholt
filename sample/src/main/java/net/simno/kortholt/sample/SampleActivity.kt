package net.simno.kortholt.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.simno.kortholt.kortholt

class SampleActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch { kortholt.openPatch(R.raw.test, "test.pd", extractZip = true) }
        setContent {
            MaterialTheme {
                Surface {
                    SampleScreen()
                }
            }
        }
    }

    override fun onDestroy() {
        stopService(SampleService.intent(this))
        runBlocking {
            kortholt.stopStream()
            kortholt.closePatch()
        }
        super.onDestroy()
    }
}
