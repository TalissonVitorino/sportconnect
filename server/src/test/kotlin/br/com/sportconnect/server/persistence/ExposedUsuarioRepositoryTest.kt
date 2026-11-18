package br.com.sportconnect.server.persistence

import br.com.sportconnect.server.config.AppConfig
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ExposedUsuarioRepositoryTest {
    private val config = AppConfig.fromEnv(
        mapOf(
            "JWT_SECRET" to "x",
            "DATABASE_URL" to PostgresTestContainer.instancia.jdbcUrl,
            "DATABASE_USUARIO" to PostgresTestContainer.instancia.username,
            "DATABASE_SENHA" to PostgresTestContainer.instancia.password,
        ),
    ).also { migrar(it) }
    private val db = conectar(config)
    private val repo = ExposedUsuarioRepository(db)

    @BeforeTest
    fun limparTabelas() {
        // O container/schema é compartilhado entre toda a suíte do módulo (PostgresTestContainer),
        // então cada classe que escreve em usuarios/refresh_tokens precisa limpar suas próprias
        // linhas antes de cada teste, independentemente da ordem de execução.
        transaction(db) {
            RefreshTokensTable.deleteAll()
            UsuariosTable.deleteAll()
        }
    }

    @Test
    fun cria_e_busca_por_celular() = runBlocking {
        val criado = repo.criar(celular = "+5511988887777", nome = "Convidado")
        val encontrado = repo.buscarPorCelular("+5511988887777")
        assertEquals(criado.id, encontrado?.id)
    }

    @Test
    fun busca_por_celular_inexistente_devolve_null() = runBlocking {
        assertNull(repo.buscarPorCelular("+5511900000000"))
    }
}
