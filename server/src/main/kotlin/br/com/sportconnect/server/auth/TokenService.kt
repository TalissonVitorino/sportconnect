package br.com.sportconnect.server.auth

import br.com.sportconnect.server.config.AppConfig
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import java.util.Date
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes

/** Par de tokens emitido ao confirmar OTP (docs/15-contrato-api.md §1). */
data class ParDeTokens(val acesso: String, val renovacao: String, val expiraEm: Instant)

/**
 * Emite os JWTs de acesso e renovação, assinados com HMAC256 usando [AppConfig.jwtSecret].
 *
 * Os dois tipos de token têm o mesmo emissor e a mesma chave de assinatura, então levam uma claim
 * [CLAIM_TIPO] (`"acesso"` ou `"renovacao"`) para que uma rota protegida (Task 6) possa recusar um
 * refresh token de 30 dias que, sem essa claim, passaria como token de acesso válido.
 *
 * Esta task só emite tokens: a verificação de um JWT numa rota protegida é Task 6, e a
 * persistência/rotação do refresh token na tabela `refresh_tokens` não é feita aqui — nada neste
 * esqueleto ainda chama `POST /auth/token/renovar`.
 */
class TokenService(private val config: AppConfig) {

    fun emitirAcesso(usuarioId: String): String =
        criarToken(usuarioId, TIPO_ACESSO, Clock.System.now() + config.acessoTtlMinutos.minutes)

    fun emitirRefresh(usuarioId: String): ParDeTokens {
        val expiraEm = Clock.System.now() + config.acessoTtlMinutos.minutes
        val acesso = criarToken(usuarioId, TIPO_ACESSO, expiraEm)
        val renovacao = criarToken(usuarioId, TIPO_RENOVACAO, Clock.System.now() + config.refreshTtlDias.days)
        return ParDeTokens(acesso = acesso, renovacao = renovacao, expiraEm = expiraEm)
    }

    private fun criarToken(usuarioId: String, tipo: String, expiraEm: Instant): String =
        JWT.create()
            .withIssuer(config.jwtIssuer)
            .withClaim("usuarioId", usuarioId)
            .withClaim(CLAIM_TIPO, tipo)
            .withExpiresAt(Date(expiraEm.toEpochMilliseconds()))
            .sign(Algorithm.HMAC256(config.jwtSecret))

    companion object {
        /** Nome da claim que distingue token de acesso de token de renovação. */
        const val CLAIM_TIPO = "tipo"
        const val TIPO_ACESSO = "acesso"
        const val TIPO_RENOVACAO = "renovacao"
    }
}
