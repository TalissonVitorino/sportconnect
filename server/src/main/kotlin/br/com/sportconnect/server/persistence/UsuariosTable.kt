package br.com.sportconnect.server.persistence

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

/**
 * Mapeamento Exposed da tabela `usuarios` (docs/16-modelo-dados.md §2).
 *
 * Modela, por ora, apenas as colunas gravadas por este esqueleto (`id`, `celular`, `nome`,
 * `criado_em`). As demais colunas da tabela (`email`, `cpf`, `papeis`, `arena_id`, ...) já
 * existem no banco via migração, mas ainda não são mapeadas aqui — [ExposedUsuarioRepository]
 * devolve o `Usuario` completo (Task 5) preenchendo essas colunas com os defaults do `data
 * class` (null/emptyList/`setOf(ATLETA)`) em vez de lê-las do banco.
 */
object UsuariosTable : Table("usuarios") {
    val id = uuid("id")
    val celular = text("celular").uniqueIndex()
    val nome = text("nome")
    val criadoEm = timestamp("criado_em")

    override val primaryKey = PrimaryKey(id)
}
