package net.simno.kortholt

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import net.simno.kortholt.databinding.SampleActivityBinding
import org.puredata.core.PdBase

class SampleActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = SampleActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        PdBaseHelper.openPatch(this, R.raw.test, "test.pd")

        binding.playBox.setOnCheckedChangeListener { _, isChecked ->
            SampleService.intent(this).let { intent ->
                if (isChecked) {
                    ContextCompat.startForegroundService(this, intent)
                } else {
                    stopService(intent)
                }
            }
        }
        binding.leftBox.setOnCheckedChangeListener { _, isChecked ->
            PdBase.sendFloat("left", if (isChecked) 1F else 0F)
        }
        binding.rightBox.setOnCheckedChangeListener { _, isChecked ->
            PdBase.sendFloat("right", if (isChecked) 1F else 0F)
        }
    }

    override fun onDestroy() {
        stopService(SampleService.intent(this))
        PdBaseHelper.closePatch()
        super.onDestroy()
    }
}
