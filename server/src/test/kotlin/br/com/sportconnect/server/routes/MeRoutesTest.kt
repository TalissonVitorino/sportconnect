package br.com.sportconnect.server.routes

import br.com.sportconnect.server.auth.InMemoryOtpStore
import br.com.sportconnect.server.auth.OtpStore
import br.com.sportconnect.server.auth.TokenService
import br.com.sportconnect.server.config.AppConfig
import br.com.sportconnect.server.module
import br.com.sportconnect.server.persistence.PostgresTestContainer
import br.com.sportconnect.server.persistence.RefreshTokensTable
import br.com.sportconnect.server.persistence.UsuariosTable
import br.com.sportconnect.server.persistence.criarDataSource
import br.com.sportconnect.server.persistence.migrar
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.AfterClass
import org.koin.ktor.ext.getKoin
import java.util.Date
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes

/**
 * Cobre Review Focus #4 (ver task-6-brief.md): `GET /me` exige um token de ACESSO válido.
 *
 * A checagem mais importante aqui é [me_com_token_de_renovacao_devolve_401_nao_autenticado] — sem
 * ela, um refresh token de 30 dias (mesmo emissor/assinatura de um token de acesso) passaria como
 * válido nesta rota, o que anularia o propósito de ter tokens de acesso de vida curta.
 */
class MeRoutesTest {

    companion object {
        // Mesmo padrão de AuthRoutesTest/ExposedUsuarioRepositoryTest: pool próprio desta classe.
        private val config = AppConfig.fromEnv(
            mapOf(
                "JWT_SECRET" to "segredo-de-teste-nao-usar-em-producao",
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

    @BeforeTest
    fun limparTabelas() {
        transaction(db) {
            RefreshTokensTable.deleteAll()
            UsuariosTable.deleteAll()
        }
    }

    /** Executa o fluxo completo OTP -> confirmar e devolve apenas o token de ACESSO emitido. */
    private suspend fun ApplicationTestBuilder.tokensDoFluxoDeAuth(): String {
        val celular = "+5511900000001"
        client.post("/auth/otp") {
            contentType(ContentType.Application.Json)
            setBody("""{"celular":"$celular","canal":"SMS"}""")
        }
        val otpStore = application.getKoin().get<OtpStore>() as InMemoryOtpStore
        val codigo = otpStore.ultimoCodigoPara(celular)!!
        val resposta = client.post("/auth/otp/confirmar") {
            contentType(ContentType.Application.Json)
            setBody("""{"celular":"$celular","codigo":"$codigo"}""")
        }
        assertEquals(HttpStatusCode.OK, resposta.status)
        val corpo = resposta.bodyAsText()
        return Regex(""""acesso":"([^"]+)"""").find(corpo)!!.groupValues[1]
    }

    private fun tokenComTipo(tipo: String, expiraEm: Date): String =
        JWT.create()
            .withIssuer(config.jwtIssuer)
            .withClaim("usuarioId", "qualquer-id")
            .withClaim(TokenService.CLAIM_TIPO, tipo)
            .withExpiresAt(expiraEm)
            .sign(Algorithm.HMAC256(config.jwtSecret))

    @Test
    fun me_com_token_valido_devolve_o_usuario() = testApplication {
        application { module(config) }
        val acesso = tokensDoFluxoDeAuth()

        val resposta = client.get("/me") { header(HttpHeaders.Authorization, "Bearer $acesso") }

        assertEquals(HttpStatusCode.OK, resposta.status)
    }

    @Test
    fun me_sem_token_devolve_401_nao_autenticado() = testApplication {
        application { module(config) }

        val resposta = client.get("/me")

        assertEquals(HttpStatusCode.Unauthorized, resposta.status)
        assertTrue(resposta.bodyAsText().contains("NAO_AUTENTICADO"))
    }

    @Test
    fun me_com_token_malformado_devolve_401_nao_autenticado() = testApplication {
        application { module(config) }

        val resposta = client.get("/me") { header(HttpHeaders.Authorization, "Bearer isto-nao-e-um-jwt") }

        assertEquals(HttpStatusCode.Unauthorized, resposta.status)
        assertTrue(resposta.bodyAsText().contains("NAO_AUTENTICADO"))
    }

    @Test
    fun me_com_token_expirado_devolve_401_nao_autenticado() = testApplication {
        application { module(config) }
        val expirado = tokenComTipo(
            TokenService.TIPO_ACESSO,
            Date((Clock.System.now() - 1.minutes).toEpochMilliseconds()),
        )

        val resposta = client.get("/me") { header(HttpHeaders.Authorization, "Bearer $expirado") }

        assertEquals(HttpStatusCode.Unauthorized, resposta.status)
        assertTrue(resposta.bodyAsText().contains("NAO_AUTENTICADO"))
    }

    @Test
    fun me_com_token_de_renovacao_devolve_401_nao_autenticado() = testApplication {
        application { module(config) }
        // Token com assinatura/emissor válidos e NÃO expirado, mas tipo = "renovacao" (refresh
        // token) — exatamente o caso que a checagem explícita de `tipo` precisa recusar.
        val renovacao = tokenComTipo(
            TokenService.TIPO_RENOVACAO,
            Date((Clock.System.now() + 30.days).toEpochMilliseconds()),
        )

        val resposta = client.get("/me") { header(HttpHeaders.Authorization, "Bearer $renovacao") }

        assertEquals(HttpStatusCode.Unauthorized, resposta.status)
        assertTrue(resposta.bodyAsText().contains("NAO_AUTENTICADO"))
    }

    // Findings 3 e 4 (review final): um JWT de acesso valido (assinatura/emissor/tipo OK, nao
    // expirado) cujo `usuarioId` nao existe em `usuarios` - usuario deletado/anonimizado via LGPD
    // enquanto o token ainda era tecnicamente valido - nao pode inventar um codigo de erro novo
    // (`USUARIO_NAO_ENCONTRADO`, 404) que nao existe no catalogo (docs/15 §12). O cliente so sabe
    // tratar o 401 NAO_AUTENTICADO universal para "esta sessao nao e mais valida".
    @Test
    fun me_com_usuario_id_inexistente_devolve_401_nao_autenticado() = testApplication {
        application { module(config) }
        val tokenService = TokenService(config)
        val usuarioIdInexistente = java.util.UUID.randomUUID().toString()
        val acesso = tokenService.emitirAcesso(usuarioIdInexistente)

        val resposta = client.get("/me") { header(HttpHeaders.Authorization, "Bearer $acesso") }

        assertEquals(HttpStatusCode.Unauthorized, resposta.status)
        assertTrue(resposta.bodyAsText().contains("NAO_AUTENTICADO"))
    }
}
