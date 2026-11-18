package br.com.sportconnect.server.persistence

import br.com.sportconnect.server.config.AppConfig
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.AfterClass
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ExposedUsuarioRepositoryTest {

    companion object {
        // kotlin.test/JUnit4 cria uma instância desta classe por método de teste; se o pool
        // HikariCP fosse aberto em uma propriedade de instância (como em `conectar(config)`),
        // cada método de teste abriria um `HikariPool` novo que nunca seria fechado. Em um
        // módulo com uma classe de teste isso é inofensivo, mas o `PostgresTestContainer` é
        // compartilhado por várias classes de teste (Tasks 5 e 6 também vão usá-lo), então os
        // pools se acumulariam contra o mesmo container ao longo da suíte. Por isso o pool é
        // criado uma única vez por classe (companion object) e fechado explicitamente em
        // [encerrarPool] quando a classe termina — mesmo padrão que outras classes de teste de
        // persistência do módulo devem seguir.
        private val config = AppConfig.fromEnv(
            mapOf(
                "JWT_SECRET" to "x",
                "DATABASE_URL" to PostgresTestContainer.instancia.jdbcUrl,
                "DATABASE_USUARIO" to PostgresTestContainer.instancia.username,
                "DATABASE_SENHA" to PostgresTestContainer.instancia.password,
            ),
        ).also { migrar(it) }
        private val dataSource = criarDataSource(config)
        private val db: Database = Database.connect(dataSource)

        @JvmStatic
        @AfterClass
        fun encerrarPool() {
            dataSource.close()
        }
    }

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
