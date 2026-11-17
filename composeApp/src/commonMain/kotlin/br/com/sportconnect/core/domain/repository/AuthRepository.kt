package br.com.sportconnect.core.domain.repository

import br.com.sportconnect.core.model.Usuario
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val currentUser: StateFlow<Usuario?>
    suspend fun solicitarOtp(celular: String): Result<Unit>
    suspend fun verificarOtp(celular: String, codigo: String): Result<Usuario>
    suspend fun logout()
    /** Demo: alterna entre perfis (organizador, atleta, gestor de arena). */
    suspend fun entrarComo(usuarioId: String): Result<Usuario>
    suspend fun usuario(id: String): Usuario?
}
