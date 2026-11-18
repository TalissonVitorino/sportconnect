package br.com.sportconnect.core

import br.com.sportconnect.core.data.fake.DemoData
import br.com.sportconnect.core.data.fake.FakeAuthRepository
import br.com.sportconnect.core.data.fake.FakeGameRepository
import br.com.sportconnect.core.data.fake.InMemoryAnalyticsTracker
import br.com.sportconnect.core.data.local.AppPreferences
import br.com.sportconnect.core.domain.repository.AuthFailure
import br.com.sportconnect.core.domain.usecase.DeleteAccountUseCase
import br.com.sportconnect.core.domain.usecase.ExportMyDataUseCase
import com.russhwolf.settings.MapSettings
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Instant

/** Exclusao de conta (US-003), exportacao de dados (RNF-11) e edicao de perfil (B11, B12, B13). */
class AccountUseCasesTest {
    private val clock = TestClock(Instant.parse("2026-11-17T12:00:00Z"))
    private val seed = DemoData(clock)
    private val analytics = InMemoryAnalyticsTracker()
    private val prefs = AppPreferences(MapSettings())
    private val games = FakeGameRepository(seed)
    private val auth = FakeAuthRepository(seed, analytics, clock, prefs)
    private val delete = DeleteAccountUseCase(games, auth, analytics)
    private val export = ExportMyDataUseCase(games, auth, analytics, clock)

    private suspend fun loginNovo(celular: String = "11912345678") {
        auth.solicitarOtp(celular)
        auth.verificarOtp(celular, FakeAuthRepository.CODIGO_OTP_DEMO).getOrThrow()
    }

    @Test
    fun conta_sem_jogo_pago_e_excluida_na_hora_e_desloga() = runTest {
        loginNovo()
        assertTrue(delete.jogosQueSeguram().first().isEmpty())
        val r = delete().getOrThrow()
        assertNull(r.agendadaApos)
        assertNull(auth.currentUser.value)
        assertNull(prefs.session)
        assertTrue(analytics.nomes().contains("conta_exclusao_solicitada"))
    }

    @Test
    fun com_cota_paga_em_jogo_ativo_a_exclusao_fica_para_depois_do_jogo() = runTest {
        auth.entrarComo("u-organizador").getOrThrow()
        val pendentes = delete.jogosQueSeguram().first()
        assertTrue(pendentes.isNotEmpty(), "o organizador da demo tem cota paga no Racha")
        val r = delete().getOrThrow()
        assertEquals(pendentes.maxOf { it.fim }, r.agendadaApos)
        assertNull(auth.currentUser.value)
    }

    @Test
    fun exportacao_traz_perfil_jogos_e_pagamentos_em_json() = runTest {
        auth.entrarComo("u-organizador").getOrThrow()
        val json = Json.parseToJsonElement(export().getOrThrow()).jsonObject
        assertEquals("u-organizador", json["perfil"]!!.jsonObject["id"]!!.jsonPrimitive.content)
        assertTrue(json["jogos"]!!.jsonArray.isNotEmpty())
        assertTrue(json["pagamentos"]!!.jsonArray.isNotEmpty())
        assertTrue(analytics.nomes().contains("dados_exportados"))
    }

    @Test
    fun email_invalido_e_recusado_e_valido_fica_salvo_na_sessao() = runTest {
        loginNovo()
        assertTrue(auth.atualizarPerfil("Talisson", "nao-e-email").isFailure)
        auth.atualizarPerfil("Talisson", "talisson@email.com").getOrThrow()
        assertEquals("talisson@email.com", FakeAuthRepository(seed, analytics, clock, prefs).currentUser.value?.email)
    }

    @Test
    fun troca_de_celular_exige_codigo_enviado_para_o_numero_novo() = runTest {
        loginNovo()
        // Sem pedir codigo para o numero novo, nao troca.
        assertIs<AuthFailure.CodeExpired>(auth.trocarCelular("21987654321", FakeAuthRepository.CODIGO_OTP_DEMO).exceptionOrNull())
        auth.solicitarOtp("21987654321")
        assertIs<AuthFailure.CodeInvalid>(auth.trocarCelular("21987654321", "000000").exceptionOrNull())
        val u = auth.trocarCelular("21987654321", FakeAuthRepository.CODIGO_OTP_DEMO).getOrThrow()
        assertEquals("+5521987654321", u.celular)
        assertNotNull(prefs.session?.celular?.takeIf { it.endsWith("21987654321") })
    }
}
