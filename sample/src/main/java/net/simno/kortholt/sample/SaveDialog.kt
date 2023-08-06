package net.simno.kortholt.sample

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.CreateDocument
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val WAV_MIME_TYPE = "audio/x-wav"

@Composable
fun SaveDialog(
    waveFile: File,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val updatedOnDismiss by rememberUpdatedState(onDismiss)

    val shareLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        updatedOnDismiss()
    }

    val saveLauncher = rememberLauncherForActivityResult(CreateDocument(WAV_MIME_TYPE)) { uri ->
        if (uri != null) {
            scope.launch(Dispatchers.IO) {
                context.contentResolver.openOutputStream(uri)?.use {
                    it.write(waveFile.readBytes())
                }
                updatedOnDismiss()
            }
        }
    }

    AlertDialog(
        onDismissRequest = updatedOnDismiss,
        title = {
            Text(text = waveFile.name)
        },
        dismissButton = {
            Button(
                onClick = { saveLauncher.launch(waveFile.name) },
                content = { Text("Save") }
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    val uri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID, waveFile)

                    val target = Intent(Intent.ACTION_SEND).apply {
                        setDataAndType(uri, WAV_MIME_TYPE)
                        putExtra(Intent.EXTRA_STREAM, uri)
                        putExtra(Intent.EXTRA_SUBJECT, waveFile.name)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }

                    val intent = Intent.createChooser(target, "Share")
                    shareLauncher.launch(intent)
                },
                content = { Text("Share") }
            )
        }
    )
}
