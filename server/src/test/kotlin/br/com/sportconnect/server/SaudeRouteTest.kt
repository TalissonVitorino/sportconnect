package br.com.sportconnect.server

import br.com.sportconnect.server.config.AppConfig
import br.com.sportconnect.server.persistence.PostgresTestContainer
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals

class SaudeRouteTest {

    // Desde a Task 5, module() monta persistência (Postgres via HikariCP/Exposed) além das rotas,
    // então mesmo este teste só de /saude precisa de um AppConfig apontando para um banco real —
    // reaproveita o mesmo PostgresTestContainer compartilhado das Tasks 4-6 (/saude não grava em
    // usuarios/refresh_tokens, então não precisa de limpeza de tabelas entre testes).
    private val config = AppConfig.fromEnv(
        mapOf(
            "JWT_SECRET" to "segredo-de-teste-nao-usar-em-producao",
            "DATABASE_URL" to PostgresTestContainer.instancia.jdbcUrl,
            "DATABASE_USUARIO" to PostgresTestContainer.instancia.username,
            "DATABASE_SENHA" to PostgresTestContainer.instancia.password,
        ),
    )

    @Test
    fun saude_responde_200_ok() = testApplication {
        application { module(config) }
        val resposta = client.get("/saude")
        assertEquals(HttpStatusCode.OK, resposta.status)
        assertEquals("""{"status":"ok"}""", resposta.bodyAsText())
    }
}
