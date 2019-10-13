package net.simno.kortholt

import android.os.Bundle
import android.widget.CheckBox
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import org.puredata.core.PdBase

class SampleActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sample)

        PdBaseHelper.openPatch(this, R.raw.test, "test.pd")

        findViewById<CheckBox>(R.id.play_box).setOnCheckedChangeListener { _, isChecked ->
            SampleService.intent(this).let { intent ->
                if (isChecked) {
                    ContextCompat.startForegroundService(this, intent)
                } else {
                    stopService(intent)
                }
            }
        }
        findViewById<CheckBox>(R.id.left_box).setOnCheckedChangeListener { _, isChecked ->
            PdBase.sendFloat("left", if (isChecked) 1F else 0F)
        }
        findViewById<CheckBox>(R.id.right_box).setOnCheckedChangeListener { _, isChecked ->
            PdBase.sendFloat("right", if (isChecked) 1F else 0F)
        }
    }

    override fun onDestroy() {
        stopService(SampleService.intent(this))
        PdBaseHelper.closePatch()
        super.onDestroy()
    }
}
