package br.com.sportconnect.server.persistence

import br.com.sportconnect.core.model.Usuario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

class ExposedUsuarioRepository(private val db: Database) : UsuarioRepository {

    override suspend fun buscarPorCelular(celular: String): Usuario? = withContext(Dispatchers.IO) {
        transaction(db) {
            UsuariosTable.selectAll()
                .where { UsuariosTable.celular eq celular }
                .map(::paraUsuario)
                .singleOrNull()
        }
    }

    override suspend fun buscarPorId(id: String): Usuario? = withContext(Dispatchers.IO) {
        transaction(db) {
            UsuariosTable.selectAll()
                .where { UsuariosTable.id eq UUID.fromString(id) }
                .map(::paraUsuario)
                .singleOrNull()
        }
    }

    override suspend fun criar(celular: String, nome: String): Usuario = withContext(Dispatchers.IO) {
        transaction(db) {
            val id = UUID.randomUUID()
            val criadoEm = Clock.System.now()
            UsuariosTable.insert {
                it[UsuariosTable.id] = id
                it[UsuariosTable.celular] = celular
                it[UsuariosTable.nome] = nome
                it[UsuariosTable.criadoEm] = criadoEm
            }
            Usuario(
                id = id.toString(),
                nome = nome,
                celular = celular,
                criadoEm = criadoEm.paraInstantDaStdlib(),
            )
        }
    }

    private fun paraUsuario(row: ResultRow): Usuario = Usuario(
        id = row[UsuariosTable.id].toString(),
        nome = row[UsuariosTable.nome],
        celular = row[UsuariosTable.celular],
        criadoEm = row[UsuariosTable.criadoEm].paraInstantDaStdlib(),
    )

    /**
     * [Usuario.criadoEm] usa `kotlin.time.Instant` (tipo portado verbatim de `core/model`), mas o
     * Exposed grava via `kotlinx.datetime.Instant` (fixado numa versão anterior à depreciação —
     * ver comentário em `gradle/libs.versions.toml`). A conversão por milissegundos é suficiente
     * aqui: `criado_em` não precisa de precisão de nanossegundos.
     */
    private fun Instant.paraInstantDaStdlib(): kotlin.time.Instant =
        kotlin.time.Instant.fromEpochMilliseconds(this.toEpochMilliseconds())
}
