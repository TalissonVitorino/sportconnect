package br.com.sportconnect.server.persistence

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

class ExposedUsuarioRepository(private val db: Database) : UsuarioRepository {

    override suspend fun buscarPorCelular(celular: String): UsuarioResumo? = withContext(Dispatchers.IO) {
        transaction(db) {
            UsuariosTable.selectAll()
                .where { UsuariosTable.celular eq celular }
                .map(::paraUsuarioResumo)
                .singleOrNull()
        }
    }

    override suspend fun buscarPorId(id: String): UsuarioResumo? = withContext(Dispatchers.IO) {
        transaction(db) {
            UsuariosTable.selectAll()
                .where { UsuariosTable.id eq UUID.fromString(id) }
                .map(::paraUsuarioResumo)
                .singleOrNull()
        }
    }

    override suspend fun criar(celular: String, nome: String): UsuarioResumo = withContext(Dispatchers.IO) {
        transaction(db) {
            val id = UUID.randomUUID()
            val criadoEm = Clock.System.now()
            UsuariosTable.insert {
                it[UsuariosTable.id] = id
                it[UsuariosTable.celular] = celular
                it[UsuariosTable.nome] = nome
                it[UsuariosTable.criadoEm] = criadoEm
            }
            UsuarioResumo(id = id.toString(), celular = celular, nome = nome, criadoEm = criadoEm)
        }
    }

    private fun paraUsuarioResumo(row: ResultRow): UsuarioResumo = UsuarioResumo(
        id = row[UsuariosTable.id].toString(),
        celular = row[UsuariosTable.celular],
        nome = row[UsuariosTable.nome],
        criadoEm = row[UsuariosTable.criadoEm],
    )
}
