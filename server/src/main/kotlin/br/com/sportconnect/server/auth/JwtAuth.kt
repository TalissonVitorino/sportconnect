package br.com.sportconnect.server.auth

import br.com.sportconnect.server.config.AppConfig
import br.com.sportconnect.server.http.CatalogoErros
import br.com.sportconnect.server.http.ErroCorpo
import br.com.sportconnect.server.http.ErroEnvelope
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.ContentType
import io.ktor.server.auth.AuthenticationConfig
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.response.respondText
import kotlinx.serialization.json.Json

/**
 * Provider de autenticação `"jwt"`: valida emissor, assinatura HMAC256 ([AppConfig.jwtSecret]) e
 * expiração (tudo isso automaticamente, via [com.auth0.jwt.interfaces.JWTVerifier]) — e, além
 * disso, recusa explicitamente qualquer token cuja claim [TokenService.CLAIM_TIPO] não seja
 * [TokenService.TIPO_ACESSO].
 *
 * Essa última checagem é o motivo desta função existir: [TokenService] assina os tokens de acesso
 * e de renovação com o MESMO segredo e o MESMO emissor (só a claim `tipo` os distingue), então sem
 * `withClaim(TokenService.CLAIM_TIPO, TokenService.TIPO_ACESSO)` no verifier, um refresh token de
 * 30 dias passaria como token de acesso válido em qualquer rota protegida com este provider.
 *
 * O `challenge` (chamado quando a autenticação falha — token ausente, malformado, expirado, ou com
 * `tipo` errado) responde com o mesmo envelope de erro padronizado em `server.http`
 * (`{"erro":{"codigo":"NAO_AUTENTICADO","mensagem":"..."}}`, 401) em vez do corpo default do Ktor.
 */
fun AuthenticationConfig.jwtAuth(config: AppConfig) {
    jwt("jwt") {
        verifier(
            JWT.require(Algorithm.HMAC256(config.jwtSecret))
                .withIssuer(config.jwtIssuer)
                .withClaim(TokenService.CLAIM_TIPO, TokenService.TIPO_ACESSO)
                .build(),
        )
        validate { credential ->
            val usuarioId = credential.payload.getClaim("usuarioId").asString()
            if (usuarioId.isNullOrBlank()) null else JWTPrincipal(credential.payload)
        }
        challenge { _, _ ->
            val envelope = ErroEnvelope(ErroCorpo(codigo = "NAO_AUTENTICADO", mensagem = "Token ausente, invalido ou expirado."))
            call.respondText(
                Json.encodeToString(envelope),
                ContentType.Application.Json,
                CatalogoErros.statusPara("NAO_AUTENTICADO"),
            )
        }
    }
}
