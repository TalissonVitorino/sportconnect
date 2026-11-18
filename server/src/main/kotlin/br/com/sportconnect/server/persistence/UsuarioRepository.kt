package br.com.sportconnect.server.persistence

import kotlinx.datetime.Instant

/**
 * Visão resumida de `usuarios` usada até a Task 5 portar o `Usuario` completo de `core/model`
 * para este módulo.
 */
data class UsuarioResumo(
    val id: String,
    val celular: String,
    val nome: String,
    val criadoEm: Instant,
)

interface UsuarioRepository {
    suspend fun buscarPorCelular(celular: String): UsuarioResumo?
    suspend fun buscarPorId(id: String): UsuarioResumo?
    suspend fun criar(celular: String, nome: String): UsuarioResumo
}
