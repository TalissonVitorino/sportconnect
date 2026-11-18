package br.com.sportconnect.server.http

import br.com.sportconnect.core.domain.usecase.RegraNegocioException
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals

class TratamentoDeErrosTest {
    @Test
    fun regra_negocio_vira_envelope_com_status_do_catalogo() = testApplication {
        application {
            configurarTratamentoDeErros()
            routing { get("/_teste-regra") { throw RegraNegocioException("CODE_INVALID", "Código inválido.") } }
        }
        val resposta = client.get("/_teste-regra")
        assertEquals(HttpStatusCode.BadRequest, resposta.status)
        assertEquals("""{"erro":{"codigo":"CODE_INVALID","mensagem":"Código inválido."}}""", resposta.bodyAsText())
    }

    @Test
    fun exception_generica_vira_500_sem_vazar_detalhe() = testApplication {
        application {
            configurarTratamentoDeErros()
            routing { get("/_teste-boom") { error("detalhe interno sensível") } }
        }
        val resposta = client.get("/_teste-boom")
        assertEquals(HttpStatusCode.InternalServerError, resposta.status)
        assertEquals("""{"erro":{"codigo":"ERRO_INTERNO","mensagem":"Erro interno. Tente novamente."}}""", resposta.bodyAsText())
    }
}
