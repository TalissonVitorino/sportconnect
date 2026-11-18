package br.com.sportconnect.core.data.local

import androidx.compose.ui.graphics.ImageBitmap
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.decodeToImageBitmap

/**
 * Fotos da arena (E9, CT-01): ate [MAX] por arena, reduzidas para 1600px (JPEG 85%) e guardadas no app.
 * Com o back-end real, [add] envia para o armazenamento da arena e todos os atletas veem.
 */
class ArenaPhotoStore(private val settings: Settings) {
    private val memory = mutableMapOf<String, MutableList<ByteArray>>()
    private val _photos = MutableStateFlow<Map<String, List<ImageBitmap>>>(emptyMap())
    val photos: StateFlow<Map<String, List<ImageBitmap>>> = _photos.asStateFlow()

    private fun countKey(arenaId: String) = "sportconnect.arena_photos_$arenaId"
    private fun name(arenaId: String, i: Int) = "arena-$arenaId-$i.jpg"

    suspend fun load(arenaId: String) {
        val bytes = memory[arenaId] ?: (0 until settings.getInt(countKey(arenaId), 0)).mapNotNull { i ->
            runCatching { PhotoFiles.read(name(arenaId, i)) }.getOrNull()
        }
        _photos.update { it + (arenaId to bytes.mapNotNull { b -> b.toBitmap() }) }
    }

    /** Adiciona fotos (ate completar [MAX]). Retorna quantas foram aceitas. */
    suspend fun add(arenaId: String, originals: List<ByteArray>): Int {
        load(arenaId)
        val current = currentBytes(arenaId).toMutableList()
        var accepted = 0
        for (o in originals) {
            if (current.size >= MAX) break
            val b = runCatching { PhotoFiles.compress(o, 1600) }.getOrElse { o }
            if (b.toBitmap() == null) continue
            current += b
            accepted++
        }
        persist(arenaId, current)
        return accepted
    }

    suspend fun remove(arenaId: String, index: Int) {
        val current = currentBytes(arenaId).toMutableList()
        if (index !in current.indices) return
        current.removeAt(index)
        persist(arenaId, current)
    }

    private suspend fun currentBytes(arenaId: String): List<ByteArray> = memory[arenaId]
        ?: (0 until settings.getInt(countKey(arenaId), 0)).mapNotNull { i -> runCatching { PhotoFiles.read(name(arenaId, i)) }.getOrNull() }

    private suspend fun persist(arenaId: String, list: List<ByteArray>) {
        val old = settings.getInt(countKey(arenaId), 0)
        var onDisk = true
        list.forEachIndexed { i, b -> if (!runCatching { PhotoFiles.write(name(arenaId, i), b) }.getOrDefault(false)) onDisk = false }
        (list.size until old).forEach { i -> runCatching { PhotoFiles.delete(name(arenaId, i)) } }
        if (onDisk) {
            memory.remove(arenaId)
            settings.putInt(countKey(arenaId), list.size)
        } else {
            memory[arenaId] = list.toMutableList()
        }
        _photos.update { it + (arenaId to list.mapNotNull { b -> b.toBitmap() }) }
    }

    @OptIn(ExperimentalResourceApi::class)
    private fun ByteArray.toBitmap(): ImageBitmap? = runCatching { decodeToImageBitmap() }.getOrNull()

    companion object { const val MAX = 5 }
}
