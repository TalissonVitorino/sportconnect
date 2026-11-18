package br.com.sportconnect.server.http

import br.com.sportconnect.core.domain.usecase.RegraNegocioException
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.ContentTransformationException
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respondText
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("br.com.sportconnect.server.http.TratamentoDeErros")

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
        // `call.receive<T>()` lança `BadRequestException` quando o corpo é JSON inválido ou está
        // faltando um campo obrigatório. Quando a requisição não tem (ou tem o) `Content-Type`
        // errado, o `ContentNegotiation` lança uma subclasse de `ContentTransformationException`
        // (`UnsupportedMediaTypeException` ou `CannotTransformContentToTypeException`, dependendo
        // do caso — nenhuma das duas é subclasse de `BadRequestException` nesta versão do Ktor,
        // então os dois handlers precisam ser separados; ver `AuthRoutesTest`). Sem eles, ambas
        // caem no catch-all `Throwable` abaixo e voltam 500 em vez de um erro de validação — um
        // corpo malformado nunca é um bug do servidor.
        exception<BadRequestException> { call, _ ->
            val envelope = ErroEnvelope(ErroCorpo(codigo = "VALIDATION_ERROR", mensagem = "Requisição inválida."))
            call.respondText(
                Json.encodeToString(envelope),
                ContentType.Application.Json,
                CatalogoErros.statusPara("VALIDATION_ERROR"),
            )
        }
        exception<ContentTransformationException> { call, _ ->
            val envelope = ErroEnvelope(ErroCorpo(codigo = "VALIDATION_ERROR", mensagem = "Requisição inválida."))
            call.respondText(
                Json.encodeToString(envelope),
                ContentType.Application.Json,
                CatalogoErros.statusPara("VALIDATION_ERROR"),
            )
        }
        exception<Throwable> { call, cause ->
            // Nunca devolver o detalhe de `cause` no corpo da resposta (vazaria detalhe interno ao
            // cliente) — mas sem logar aqui, todo bug real do servidor fica invisível em produção.
            logger.error("Erro não tratado", cause)
            val envelope = ErroEnvelope(ErroCorpo(codigo = "ERRO_INTERNO", mensagem = "Erro interno. Tente novamente."))
            call.respondText(
                Json.encodeToString(envelope),
                ContentType.Application.Json,
                HttpStatusCode.InternalServerError,
            )
        }
    }
}
