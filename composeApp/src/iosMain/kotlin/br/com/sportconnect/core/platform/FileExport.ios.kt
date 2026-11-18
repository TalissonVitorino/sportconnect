package br.com.sportconnect.core.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings
import io.github.vinceglb.filekit.dialogs.compose.rememberFileSaverLauncher
import io.github.vinceglb.filekit.write
import kotlinx.coroutines.launch

/** Dialogo "Salvar como" do sistema (FileKit); grava os bytes no destino escolhido. */
@Composable
actual fun rememberFileExport(onDone: (saved: Boolean) -> Unit): (fileName: String, extension: String, bytes: ByteArray) -> Unit {
    val done = rememberUpdatedState(onDone)
    val pending = remember { mutableStateOf<ByteArray?>(null) }
    val scope = rememberCoroutineScope()
    val launcher = rememberFileSaverLauncher(
        dialogSettings = FileKitDialogSettings.createDefault(),
        onError = { pending.value = null; done.value(false) },
    ) { file ->
        val bytes = pending.value
        pending.value = null
        if (file == null || bytes == null) {
            done.value(false)
        } else {
            scope.launch { done.value(runCatching { file.write(bytes) }.isSuccess) }
        }
    }
    return remember(launcher) {
        { name, ext, bytes ->
            pending.value = bytes
            launcher.launch(suggestedName = name, extension = ext)
        }
    }
}
