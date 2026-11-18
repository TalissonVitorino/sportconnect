package br.com.sportconnect.server.persistence

import br.com.sportconnect.core.model.Usuario

/**
 * Persistência de `usuarios`. Devolve o [Usuario] completo portado de `core/model` (Task 5) — até
 * então este repositório expunha um `UsuarioResumo` (id/celular/nome/criadoEm) porque o `Usuario`
 * real ainda não tinha sido portado para este módulo.
 *
 * [ExposedUsuarioRepository] grava/lê, por ora, apenas `id`, `celular`, `nome` e `criado_em`; os
 * demais campos de [Usuario] (`email`, `cpf`, `papeis`, `arenaId`, `avatarUrl`, `consentimentos`)
 * voltam com o default do `data class` (null/emptyList/`setOf(ATLETA)`) até alguma task futura
 * precisar persisti-los.
 */
interface UsuarioRepository {
    suspend fun buscarPorCelular(celular: String): Usuario?
    suspend fun buscarPorId(id: String): Usuario?
    suspend fun criar(celular: String, nome: String): Usuario
}
