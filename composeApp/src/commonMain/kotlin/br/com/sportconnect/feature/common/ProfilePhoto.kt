package br.com.sportconnect.feature.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.sportconnect.core.data.local.ProfilePhotoStore
import br.com.sportconnect.core.domain.repository.AuthRepository
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.readBytes
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

/** Abre a galeria para trocar a foto de perfil de quem esta logado (B7). */
val LocalPickProfilePhoto = staticCompositionLocalOf<() -> Unit> { {} }

/**
 * Cria o seletor de foto UMA vez, na raiz do app, e o expoe por [LocalPickProfilePhoto]. No iOS a FileKit
 * pede que o launcher nao viva dentro de superficies transitorias (ex.: o sheet de login).
 * Tambem recarrega a foto quando a pessoa logada muda.
 */
@Composable
fun ProfilePhotoHost(onError: () -> Unit, content: @Composable () -> Unit) {
    val store: ProfilePhotoStore = koinInject()
    val auth: AuthRepository = koinInject()
    val user by auth.currentUser.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    LaunchedEffect(user?.id) { store.load(user?.id) }
    val launcher = rememberFilePickerLauncher(type = FileKitType.Image) { file ->
        val uid = auth.currentUser.value?.id
        if (file != null && uid != null) {
            scope.launch {
                val ok = runCatching { store.save(uid, file.readBytes()) }.getOrDefault(false)
                if (!ok) onError()
            }
        }
    }
    CompositionLocalProvider(LocalPickProfilePhoto provides { launcher.launch() }) { content() }
}
