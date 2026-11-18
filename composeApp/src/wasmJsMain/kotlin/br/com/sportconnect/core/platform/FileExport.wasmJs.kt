package br.com.sportconnect.core.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.download
import kotlinx.coroutines.launch

/** Na web nao ha "Salvar como": o navegador baixa o arquivo para a pasta de downloads. */
@Composable
actual fun rememberFileExport(onDone: (saved: Boolean) -> Unit): (fileName: String, extension: String, bytes: ByteArray) -> Unit {
    val done = rememberUpdatedState(onDone)
    val scope = rememberCoroutineScope()
    return remember {
        { name, ext, bytes ->
            scope.launch { done.value(runCatching { FileKit.download(bytes, "$name.$ext") }.isSuccess) }
        }
    }
}
