package br.com.sportconnect.server.config

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class AppConfigTest {
    @Test
    fun usa_defaults_quando_env_esta_vazio() {
        val config = AppConfig.fromEnv(mapOf("JWT_SECRET" to "segredo-de-teste"))
        assertEquals(8080, config.porta)
        assertEquals(15, config.acessoTtlMinutos)
        assertEquals("sportconnect", config.jwtIssuer)
        assertEquals(30, config.refreshTtlDias)
        assertEquals("jdbc:postgresql://localhost:5432/sportconnect", config.databaseUrl)
        assertEquals("sportconnect", config.databaseUsuario)
        assertEquals("sportconnect", config.databaseSenha)
    }

    @Test
    fun le_overrides_do_env() {
        val config = AppConfig.fromEnv(mapOf("JWT_SECRET" to "x", "PORTA" to "9090"))
        assertEquals(9090, config.porta)
    }

    @Test
    fun le_overrides_do_env_para_configuracao_de_banco_de_dados() {
        val config = AppConfig.fromEnv(
            mapOf(
                "JWT_SECRET" to "x",
                "DATABASE_URL" to "jdbc:postgresql://db-host:5432/outro_banco",
                "DATABASE_USUARIO" to "outro_usuario",
                "DATABASE_SENHA" to "outra_senha",
            ),
        )
        assertEquals("jdbc:postgresql://db-host:5432/outro_banco", config.databaseUrl)
        assertEquals("outro_usuario", config.databaseUsuario)
        assertEquals("outra_senha", config.databaseSenha)
    }

    @Test
    fun exige_jwt_secret_explicito() {
        assertFailsWith<IllegalStateException> { AppConfig.fromEnv(emptyMap()) }
    }
}
