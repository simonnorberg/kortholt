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

        startService(Intent(this, SampleService::class.java))
        Kortholt.create(this)
        Kortholt.open(this, R.raw.test, "test.pd")

        findViewById<CheckBox>(R.id.left_box).setOnCheckedChangeListener { _, isChecked ->
            PdBase.sendFloat("left", if (isChecked) 1F else 0F)
        }
        findViewById<CheckBox>(R.id.right_box).setOnCheckedChangeListener { _, isChecked ->
            PdBase.sendFloat("right", if (isChecked) 1F else 0F)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Kortholt.destroy()
        stopService(Intent(this, SampleService::class.java))
    }
}
