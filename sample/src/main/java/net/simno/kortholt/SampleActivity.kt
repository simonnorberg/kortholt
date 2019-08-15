package net.simno.kortholt

import android.content.Intent
import android.os.Bundle
import android.widget.CheckBox
import androidx.appcompat.app.AppCompatActivity
import org.puredata.core.PdBase

class SampleActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sample)

        PdBaseHelper.openPatch(this, R.raw.test, "test.pd")

        findViewById<CheckBox>(R.id.play_box).setOnCheckedChangeListener { _, isChecked ->
            Intent(this, SampleService::class.java).let { intent ->
                if (isChecked) startService(intent) else stopService(intent)
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
        stopService(Intent(this, SampleService::class.java))
        PdBaseHelper.closePatch()
        super.onDestroy()
    }
}
