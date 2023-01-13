package net.simno.kortholt

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import java.io.File
import kotlin.time.DurationUnit.SECONDS
import kotlin.time.toDuration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.puredata.core.PdBase

@OptIn(ExperimentalWaveFile::class)
@Composable
fun SampleScreen() {
    val context = LocalContext.current
    val kortholt = remember { context.kortholt }
    val scope = rememberCoroutineScope()
    var isPlaying by remember { mutableStateOf(false) }
    var waveFile by remember { mutableStateOf<File?>(null) }

    fun saveWaveFile(seconds: Int) {
        scope.launch(Dispatchers.IO) {
            val dir = File(context.filesDir, "wav")
            dir.mkdirs()
            val file = File(dir, "sample_${seconds}sec.wav")

            kortholt.saveWaveFile(file, seconds.toDuration(SECONDS))
            waveFile = file

            if (isPlaying) {
                kortholt.startStream()
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        LabeledCheckbox(
            label = R.string.play_label,
            checked = isPlaying,
            onCheckedChange = { isChecked ->
                isPlaying = isChecked
                SampleService.intent(context).let { intent ->
                    if (isChecked) {
                        ContextCompat.startForegroundService(context, intent)
                        scope.launch { kortholt.startStream() }
                    } else {
                        context.stopService(intent)
                        scope.launch { kortholt.stopStream() }
                    }
                }
            }
        )
        LabeledCheckbox(
            label = R.string.left_label,
            checked = true,
            onCheckedChange = { isChecked ->
                PdBase.sendFloat("left", if (isChecked) 1f else 0f)
            }
        )
        LabeledCheckbox(
            label = R.string.right_label,
            checked = true,
            onCheckedChange = { isChecked ->
                PdBase.sendFloat("right", if (isChecked) 1f else 0f)
            }
        )
        Text(
            modifier = Modifier.padding(16.dp),
            text = "Save wave file:"
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = { saveWaveFile(1) },
                content = { Text("1 sec") }
            )
            Button(
                onClick = { saveWaveFile(2) },
                content = { Text("2 sec") }
            )
            Button(
                onClick = { saveWaveFile(4) },
                content = { Text("4 sec") }
            )
            Button(
                onClick = { saveWaveFile(8) },
                content = { Text("8 sec") }
            )
        }
    }

    waveFile?.let { file ->
        SaveDialog(file) {
            runCatching { waveFile?.delete() }
            waveFile = null
        }
    }
}
