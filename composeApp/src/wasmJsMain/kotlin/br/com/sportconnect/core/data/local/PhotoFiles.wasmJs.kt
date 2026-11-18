package br.com.sportconnect.core.data.local

/** Web: sem sistema de arquivos do app; o ProfilePhotoStore guarda a foto so na memoria da sessao. */
internal actual object PhotoFiles {
    actual suspend fun read(name: String): ByteArray? = null
    actual suspend fun write(name: String, bytes: ByteArray): Boolean = false
    actual suspend fun delete(name: String) = Unit
    actual suspend fun compress(bytes: ByteArray, maxSide: Int): ByteArray = bytes
}
