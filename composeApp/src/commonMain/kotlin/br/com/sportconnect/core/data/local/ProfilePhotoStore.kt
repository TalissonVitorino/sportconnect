package br.com.sportconnect.core.data.local

import androidx.compose.ui.graphics.ImageBitmap
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.decodeToImageBitmap

/**
 * Arquivos da foto de perfil por plataforma: Android/iOS/desktop usam a pasta privada do app (FileKit);
 * na web nao ha sistema de arquivos e [PhotoFiles] devolve null/false (a foto fica so na memoria).
 */
internal expect object PhotoFiles {
    suspend fun read(name: String): ByteArray?
    suspend fun write(name: String, bytes: ByteArray): Boolean
    suspend fun delete(name: String)
    /** Reduz para no maximo [maxSide]px em JPEG; devolve os bytes originais se nao der. */
    suspend fun compress(bytes: ByteArray, maxSide: Int): ByteArray
}

/**
 * Foto de perfil (B7): guardada no armazenamento privado do app, uma por usuario, ja reduzida para no
 * maximo 512px (JPEG 85%). Na web a foto vale so durante a sessao.
 * Com o back-end real, [save] tambem envia a foto e o `Usuario.avatarUrl` passa a apontar para ela.
 */
class ProfilePhotoStore {
    private val memory = mutableMapOf<String, ByteArray>()
    private val _photo = MutableStateFlow<ImageBitmap?>(null)
    /** Foto de quem esta logado (null = mostrar iniciais). */
    val photo: StateFlow<ImageBitmap?> = _photo.asStateFlow()

    private fun nameFor(userId: String) = "avatar-$userId.jpg"

    suspend fun load(userId: String?) {
        if (userId == null) {
            _photo.value = null
            return
        }
        val bytes = memory[userId] ?: runCatching { PhotoFiles.read(nameFor(userId)) }.getOrNull()
        _photo.value = bytes?.toBitmap()
    }

    /** Comprime, salva e publica. Retorna false se a imagem nao puder ser lida (formato estranho, corrompida). */
    suspend fun save(userId: String, original: ByteArray): Boolean {
        val bytes = runCatching { PhotoFiles.compress(original, 512) }.getOrElse { original }
        val bitmap = bytes.toBitmap() ?: return false
        if (!runCatching { PhotoFiles.write(nameFor(userId), bytes) }.getOrDefault(false)) memory[userId] = bytes
        _photo.value = bitmap
        return true
    }

    suspend fun remove(userId: String) {
        memory.remove(userId)
        runCatching { PhotoFiles.delete(nameFor(userId)) }
        _photo.value = null
    }

    @OptIn(ExperimentalResourceApi::class)
    private fun ByteArray.toBitmap(): ImageBitmap? = runCatching { decodeToImageBitmap() }.getOrNull()
}
