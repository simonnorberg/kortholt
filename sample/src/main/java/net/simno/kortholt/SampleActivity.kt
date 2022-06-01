package net.simno.kortholt

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Checkbox
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import org.puredata.core.PdBase

class SampleActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PdBaseHelper.openPatch(this, R.raw.test, "test.pd")

        setContent {
            MaterialTheme {
                Surface {
                    Column(modifier = Modifier.fillMaxSize()) {
                        LabeledCheckbox(R.string.play_label, checked = false) { isChecked ->
                            this@SampleActivity.let { context ->
                                SampleService.intent(context).let { intent ->
                                    if (isChecked) {
                                        ContextCompat.startForegroundService(context, intent)
                                    } else {
                                        stopService(intent)
                                    }
                                }
                            }
                        }
                        LabeledCheckbox(R.string.left_label, checked = true) { isChecked ->
                            PdBase.sendFloat("left", if (isChecked) 1f else 0f)
                        }
                        LabeledCheckbox(R.string.right_label, checked = true) { isChecked ->
                            PdBase.sendFloat("right", if (isChecked) 1f else 0f)
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        stopService(SampleService.intent(this))
        PdBaseHelper.closePatch()
        super.onDestroy()
    }
}

@Composable
private fun LabeledCheckbox(
    label: Int,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    var isChecked by remember { mutableStateOf(checked) }
    Row(
        modifier = Modifier
            .clickable {
                isChecked = !isChecked
                onCheckedChange(isChecked)
            }
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = isChecked,
            onCheckedChange = null,
            enabled = false
        )
        Text(stringResource(label))
    }
}
