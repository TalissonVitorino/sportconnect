package br.com.sportconnect.server.persistence

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

/**
 * Mapeamento Exposed da tabela `refresh_tokens` (docs/16-modelo-dados.md §2).
 *
 * Ainda sem repositório próprio nesta task — existe para a migração e para a limpeza de dados
 * entre testes que compartilham o [PostgresTestContainer].
 */
object RefreshTokensTable : Table("refresh_tokens") {
    val id = uuid("id")
    val usuarioId = uuid("usuario_id").references(UsuariosTable.id)
    val tokenHash = text("token_hash")
    val criadoEm = timestamp("criado_em")
    val expiraEm = timestamp("expira_em")
    val revogadoEm = timestamp("revogado_em").nullable()
    val substituidoPorId = uuid("substituido_por_id").references(id).nullable()

    override val primaryKey = PrimaryKey(id)
}
