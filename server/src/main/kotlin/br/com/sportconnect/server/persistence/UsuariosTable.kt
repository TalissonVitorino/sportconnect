package br.com.sportconnect.server.persistence

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

/**
 * Mapeamento Exposed da tabela `usuarios` (docs/16-modelo-dados.md §2).
 *
 * Modela, por ora, apenas as colunas necessárias para [UsuarioResumo] (`id`, `celular`, `nome`,
 * `criado_em`). As demais colunas da tabela (`email`, `cpf`, `papeis`, `arena_id`, ...) já
 * existem no banco via migração, mas só serão mapeadas aqui quando a Task 5 portar o `Usuario`
 * completo de `core/model`.
 */
object UsuariosTable : Table("usuarios") {
    val id = uuid("id")
    val celular = text("celular").uniqueIndex()
    val nome = text("nome")
    val criadoEm = timestamp("criado_em")

    override val primaryKey = PrimaryKey(id)
}
