package br.com.sportconnect.server.http

import io.ktor.http.HttpStatusCode

/**
 * Mapa aberto de código de erro de domínio -> HTTP status.
 *
 * Entradas relevantes às Tasks 5-6 (docs/15-contrato-api.md §12 "Catálogo de erros").
 * Código desconhecido cai no default (400).
 */
object CatalogoErros {
    fun statusPara(codigo: String): HttpStatusCode = when (codigo) {
        "NAO_AUTENTICADO" -> HttpStatusCode.Unauthorized
        "PHONE_INVALID" -> HttpStatusCode.BadRequest
        "CODE_INVALID" -> HttpStatusCode.BadRequest
        "CODE_EXPIRED" -> HttpStatusCode.Gone
        "TOO_MANY_ATTEMPTS" -> HttpStatusCode.TooManyRequests
        "PROVIDER_TOKEN_INVALID" -> HttpStatusCode.Unauthorized
        "VALIDATION_ERROR" -> HttpStatusCode.UnprocessableEntity
        else -> HttpStatusCode.BadRequest
    }
}
