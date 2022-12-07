package net.simno.kortholt

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface

class SampleActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PdBaseHelper.openPatch(this, R.raw.test, "test.pd")
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
        Kortholt.destroy()
        PdBaseHelper.closePatch()
        super.onDestroy()
    }
}
