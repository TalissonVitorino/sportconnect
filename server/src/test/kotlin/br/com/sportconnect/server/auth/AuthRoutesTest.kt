package br.com.sportconnect.server.auth

import br.com.sportconnect.server.config.AppConfig
import br.com.sportconnect.server.module
import br.com.sportconnect.server.persistence.PostgresTestContainer
import br.com.sportconnect.server.persistence.RefreshTokensTable
import br.com.sportconnect.server.persistence.UsuariosTable
import br.com.sportconnect.server.persistence.criarDataSource
import br.com.sportconnect.server.persistence.migrar
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.AfterClass
import org.koin.ktor.ext.getKoin
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AuthRoutesTest {

    companion object {
        // Mesmo padrão de `ExposedUsuarioRepositoryTest`: pool próprio desta classe, criado uma
        // única vez (companion object) e fechado em `encerrarPool` — usado só para migrar o
        // schema e limpar as tabelas entre testes. O `module(config)` exercitado em cada teste
        // abre (e fecha, via `ApplicationStopping`) seu PRÓPRIO pool contra o mesmo container.
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

    private fun corpoOtp(celular: String) = """{"celular":"$celular","canal":"SMS"}"""
    private fun corpoConfirmar(celular: String, codigo: String) = """{"celular":"$celular","codigo":"$codigo"}"""

    @Test
    fun confirmar_cria_usuario_convidado_na_primeira_vez() = testApplication {
        application { module(config) }
        val celular = "+5511988887777"

        client.post("/auth/otp") {
            contentType(ContentType.Application.Json)
            setBody(corpoOtp(celular))
        }
        val otpStore = application.getKoin().get<OtpStore>() as InMemoryOtpStore
        val codigo = otpStore.ultimoCodigoPara(celular)!!

        val resposta = client.post("/auth/otp/confirmar") {
            contentType(ContentType.Application.Json)
            setBody(corpoConfirmar(celular, codigo))
        }

        assertEquals(HttpStatusCode.OK, resposta.status)
        val corpo = Json.parseToJsonElement(resposta.bodyAsText()).jsonObject
        assertEquals("Convidado", corpo["usuario"]!!.jsonObject["nome"]!!.jsonPrimitive.content)
        assertTrue(corpo["tokens"]!!.jsonObject["acesso"]!!.jsonPrimitive.content.isNotBlank())
    }

    @Test
    fun confirmar_duas_vezes_reusa_o_mesmo_usuario_sem_duplicar() = testApplication {
        application { module(config) }
        val celular = "+5511988887788"

        suspend fun confirmarFluxoCompleto(): String {
            client.post("/auth/otp") {
                contentType(ContentType.Application.Json)
                setBody(corpoOtp(celular))
            }
            val otpStore = application.getKoin().get<OtpStore>() as InMemoryOtpStore
            val codigo = otpStore.ultimoCodigoPara(celular)!!
            val resposta = client.post("/auth/otp/confirmar") {
                contentType(ContentType.Application.Json)
                setBody(corpoConfirmar(celular, codigo))
            }
            assertEquals(HttpStatusCode.OK, resposta.status)
            val corpo = Json.parseToJsonElement(resposta.bodyAsText()).jsonObject
            return corpo["usuario"]!!.jsonObject["id"]!!.jsonPrimitive.content
        }

        val idPrimeiro = confirmarFluxoCompleto()
        val idSegundo = confirmarFluxoCompleto()
        assertEquals(idPrimeiro, idSegundo)

        val totalUsuarios = transaction(db) { UsuariosTable.selectAll().count() }
        assertEquals(1L, totalUsuarios)
    }

    @Test
    fun celular_fora_do_formato_e164_vira_phone_invalid_400() = testApplication {
        application { module(config) }

        val resposta = client.post("/auth/otp") {
            contentType(ContentType.Application.Json)
            setBody("""{"celular":"11988887777","canal":"SMS"}""")
        }

        assertEquals(HttpStatusCode.BadRequest, resposta.status)
        assertTrue(resposta.bodyAsText().contains("PHONE_INVALID"))
    }

    @Test
    fun quinta_tentativa_errada_bloqueia_com_429_e_tentarApos() = testApplication {
        application { module(config) }
        val celular = "+5511988880000"

        client.post("/auth/otp") {
            contentType(ContentType.Application.Json)
            setBody(corpoOtp(celular))
        }
        val otpStore = application.getKoin().get<OtpStore>() as InMemoryOtpStore
        val codigoReal = otpStore.ultimoCodigoPara(celular)!!
        // Garante que o "código errado" nunca coincide com o código real sorteado (evitaria uma
        // falha ínfima, mas não-determinística, do teste): troca o primeiro dígito por outro.
        val codigoErrado = ((codigoReal[0].digitToInt() + 1) % 10).toString() + codigoReal.substring(1)

        repeat(4) {
            val resposta = client.post("/auth/otp/confirmar") {
                contentType(ContentType.Application.Json)
                setBody(corpoConfirmar(celular, codigoErrado))
            }
            assertEquals(HttpStatusCode.BadRequest, resposta.status)
            assertTrue(resposta.bodyAsText().contains("CODE_INVALID"))
        }

        val quinta = client.post("/auth/otp/confirmar") {
            contentType(ContentType.Application.Json)
            setBody(corpoConfirmar(celular, codigoErrado))
        }
        assertEquals(HttpStatusCode.TooManyRequests, quinta.status)
        val corpoQuinta = quinta.bodyAsText()
        assertTrue(corpoQuinta.contains("TOO_MANY_ATTEMPTS"))
        assertTrue(corpoQuinta.contains("tentarApos"))
    }
}
