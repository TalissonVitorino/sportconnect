package br.com.sportconnect.server.http

import br.com.sportconnect.core.domain.usecase.RegraNegocioException
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respondText
import kotlinx.serialization.json.Json

/**
 * Instala o tratamento global de erros: toda exceção não tratada é convertida no envelope
 * `{"erro":{"codigo":"...","mensagem":"..."}}` com o HTTP status apropriado.
 *
 * A serialização é feita manualmente (não depende de `ContentNegotiation` estar instalado),
 * para que o caminho de erro seja autocontido mesmo se a negociação de conteúdo estiver
 * indisponível ou em falha.
 */
fun Application.configurarTratamentoDeErros() {
    install(StatusPages) {
        exception<RegraNegocioException> { call, cause ->
            val envelope = ErroEnvelope(ErroCorpo(codigo = cause.codigo, mensagem = cause.message.orEmpty()))
            call.respondText(
                Json.encodeToString(envelope),
                ContentType.Application.Json,
                CatalogoErros.statusPara(cause.codigo),
            )
        }
        exception<Throwable> { call, _ ->
            val envelope = ErroEnvelope(ErroCorpo(codigo = "ERRO_INTERNO", mensagem = "Erro interno. Tente novamente."))
            call.respondText(
                Json.encodeToString(envelope),
                ContentType.Application.Json,
                HttpStatusCode.InternalServerError,
            )
        }
    }
}
