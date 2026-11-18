package br.com.sportconnect.server

import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals

class SaudeRouteTest {
    @Test
    fun saude_responde_200_ok() = testApplication {
        application { module() }
        val resposta = client.get("/saude")
        assertEquals(HttpStatusCode.OK, resposta.status)
        assertEquals("""{"status":"ok"}""", resposta.bodyAsText())
    }
}
