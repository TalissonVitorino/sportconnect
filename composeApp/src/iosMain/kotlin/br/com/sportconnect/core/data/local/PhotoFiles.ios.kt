package br.com.sportconnect.core.data.local

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.ImageFormat
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.compressImage
import io.github.vinceglb.filekit.delete
import io.github.vinceglb.filekit.exists
import io.github.vinceglb.filekit.filesDir
import io.github.vinceglb.filekit.readBytes
import io.github.vinceglb.filekit.write

/** Pasta privada do app via FileKit (mesma implementacao em Android, iOS e desktop). */
internal actual object PhotoFiles {
    private fun file(name: String) = PlatformFile(FileKit.filesDir, name)

    actual suspend fun read(name: String): ByteArray? = file(name).takeIf { it.exists() }?.readBytes()

    actual suspend fun write(name: String, bytes: ByteArray): Boolean {
        file(name).write(bytes)
        return true
    }

    actual suspend fun delete(name: String) = file(name).delete(mustExist = false)

    actual suspend fun compress(bytes: ByteArray, maxSide: Int): ByteArray =
        FileKit.compressImage(bytes = bytes, quality = 85, maxWidth = maxSide, maxHeight = maxSide, imageFormat = ImageFormat.JPEG)
}
