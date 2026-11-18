package br.com.sportconnect.server.auth

import br.com.sportconnect.server.config.AppConfig
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class TokenServiceTest {

    private val config = AppConfig.fromEnv(
        mapOf(
            "JWT_SECRET" to "segredo-de-teste-nao-usar-em-producao",
            "ACESSO_TTL_MINUTOS" to "15",
            "REFRESH_TTL_DIAS" to "30",
        ),
    )
    private val tokenService = TokenService(config)

    @Test
    fun acesso_carrega_claim_usuarioId_e_expira_no_futuro_dentro_do_ttl_configurado() {
        val antes = System.currentTimeMillis()
        val token = tokenService.emitirAcesso("usuario-1")

        val decodificado = JWT.require(Algorithm.HMAC256(config.jwtSecret))
            .withIssuer(config.jwtIssuer)
            .build()
            .verify(token) // assinatura e issuer validos; lanca se o token ja estiver expirado

        assertEquals("usuario-1", decodificado.getClaim("usuarioId").asString())
        val expiraEmMs = decodificado.expiresAtAsInstant.toEpochMilli()
        val ttlEsperadoMs = config.acessoTtlMinutos * 60_000L
        // A expiracao deve estar perto de agora+ttl (nunca no passado, nunca muito no futuro).
        assertTrue(expiraEmMs > antes, "exp deveria estar no futuro")
        assertTrue(expiraEmMs <= antes + ttlEsperadoMs + 5_000, "exp nao deveria exceder o TTL configurado")
    }

    @Test
    fun emitirRefresh_devolve_acesso_e_renovacao_assinados_com_ttls_diferentes() {
        val par = tokenService.emitirRefresh("usuario-1")

        val verificador = JWT.require(Algorithm.HMAC256(config.jwtSecret)).withIssuer(config.jwtIssuer).build()
        val acesso = verificador.verify(par.acesso)
        val renovacao = verificador.verify(par.renovacao)

        assertEquals("usuario-1", acesso.getClaim("usuarioId").asString())
        assertEquals("usuario-1", renovacao.getClaim("usuarioId").asString())
        // O refresh dura dias; o acesso, minutos: a expiracao do refresh deve ser bem mais tardia.
        assertTrue(renovacao.expiresAtAsInstant.isAfter(acesso.expiresAtAsInstant))
        assertEquals(acesso.expiresAtAsInstant.toEpochMilli() / 1000, par.expiraEm.epochSeconds)
    }

    @Test
    fun acesso_e_renovacao_carregam_claims_tipo_diferentes() {
        // Mesmo emissor, mesma claim usuarioId, mesma chave de assinatura: sem uma claim que
        // distinga o tipo, um refresh token de 30 dias passaria como token de acesso válido numa
        // rota protegida (Task 6). `tipo` existe exatamente para impedir isso.
        val par = tokenService.emitirRefresh("usuario-1")
        val verificador = JWT.require(Algorithm.HMAC256(config.jwtSecret)).withIssuer(config.jwtIssuer).build()

        val acesso = verificador.verify(par.acesso)
        val renovacao = verificador.verify(par.renovacao)

        assertEquals(TokenService.TIPO_ACESSO, acesso.getClaim(TokenService.CLAIM_TIPO).asString())
        assertEquals(TokenService.TIPO_RENOVACAO, renovacao.getClaim(TokenService.CLAIM_TIPO).asString())

        val tokenAcessoIsolado = JWT.decode(tokenService.emitirAcesso("usuario-1"))
        assertEquals(TokenService.TIPO_ACESSO, tokenAcessoIsolado.getClaim(TokenService.CLAIM_TIPO).asString())
    }

    @Test
    fun token_assinado_com_outro_segredo_falha_na_verificacao() {
        val token = tokenService.emitirAcesso("usuario-1")
        val verificadorComSegredoErrado = JWT.require(Algorithm.HMAC256("outro-segredo")).build()
        assertFailsWith<JWTVerificationException> { verificadorComSegredoErrado.verify(token) }
    }
}
