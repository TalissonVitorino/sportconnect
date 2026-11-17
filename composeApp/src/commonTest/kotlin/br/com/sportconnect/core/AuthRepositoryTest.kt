package br.com.sportconnect.core

import br.com.sportconnect.core.data.fake.DemoData
import br.com.sportconnect.core.data.fake.FakeAuthRepository
import br.com.sportconnect.core.data.fake.InMemoryAnalyticsTracker
import br.com.sportconnect.core.data.local.AppPreferences
import br.com.sportconnect.core.domain.repository.AuthFailure
import br.com.sportconnect.core.domain.repository.AuthRules
import br.com.sportconnect.core.domain.repository.CanalOtp
import br.com.sportconnect.feature.common.UiSessionStore
import com.russhwolf.settings.MapSettings
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

/** Regras de login do US-002 e persistencia de sessao/onboarding (backlog 11, B3/B5/B6). */
class AuthRepositoryTest {
    private val inicio = Instant.parse("2026-11-17T12:00:00Z")
    private val clock = TestClock(inicio)
    private val seed = DemoData(clock)
    private val analytics = InMemoryAnalyticsTracker()
    private val settings = MapSettings()
    private val prefs = AppPreferences(settings)

    private fun repo() = FakeAuthRepository(seed, analytics, clock, prefs)

    @Test
    fun instalacao_nova_comeca_deslogada() {
        assertNull(repo().currentUser.value)
    }

    @Test
    fun sem_prefs_mantem_organizador_da_demo_para_os_testes() {
        assertNotNull(FakeAuthRepository(seed, analytics, clock).currentUser.value)
    }

    @Test
    fun sessao_sobrevive_a_reabertura_do_app() = runTest {
        val auth = repo()
        auth.solicitarOtp("11912345678")
        auth.verificarOtp("11912345678", FakeAuthRepository.CODIGO_OTP_DEMO).getOrThrow()
        auth.atualizarPerfil("  Talisson   V ").getOrThrow()

        val reaberto = repo()
        assertEquals("Talisson V", reaberto.currentUser.value?.nome)

        reaberto.logout()
        assertNull(repo().currentUser.value)
    }

    @Test
    fun codigo_expira_em_10_minutos() = runTest {
        val auth = repo()
        auth.solicitarOtp("11912345678")
        clock.agora = inicio + (AuthRules.OTP_VALIDADE_MIN + 1).minutes
        assertIs<AuthFailure.CodeExpired>(auth.verificarOtp("11912345678", FakeAuthRepository.CODIGO_OTP_DEMO).exceptionOrNull())
    }

    @Test
    fun cinco_erros_bloqueiam_por_15_minutos_e_depois_liberam() = runTest {
        val auth = repo()
        auth.solicitarOtp("11912345678")
        repeat(AuthRules.MAX_TENTATIVAS - 1) {
            assertIs<AuthFailure.CodeInvalid>(auth.verificarOtp("11912345678", "000000").exceptionOrNull())
        }
        val bloqueio = auth.verificarOtp("11912345678", "000000").exceptionOrNull()
        assertIs<AuthFailure.TooManyAttempts>(bloqueio)
        assertEquals(inicio + AuthRules.BLOQUEIO_MIN.minutes, bloqueio.retryAt)
        // Bloqueado: nem o codigo certo nem um novo envio passam.
        assertIs<AuthFailure.TooManyAttempts>(auth.verificarOtp("11912345678", FakeAuthRepository.CODIGO_OTP_DEMO).exceptionOrNull())
        assertIs<AuthFailure.TooManyAttempts>(auth.solicitarOtp("11912345678").exceptionOrNull())

        clock.agora = inicio + (AuthRules.BLOQUEIO_MIN + 1).minutes
        auth.solicitarOtp("11912345678", CanalOtp.WHATSAPP).getOrThrow()
        assertTrue(auth.verificarOtp("11912345678", FakeAuthRepository.CODIGO_OTP_DEMO).isSuccess)
        assertTrue(analytics.eventos.value.any { it.nome == "otp_solicitado" && it.propriedades["canal_otp"] == "whatsapp" })
    }

    @Test
    fun celular_invalido_falha_com_tipo_proprio() = runTest {
        assertIs<AuthFailure.PhoneInvalid>(repo().solicitarOtp("1199").exceptionOrNull())
    }

    @Test
    fun consentimentos_ficam_no_usuario_e_no_aparelho() = runTest {
        val auth = repo()
        auth.solicitarOtp("11912345678")
        auth.verificarOtp("11912345678", FakeAuthRepository.CODIGO_OTP_DEMO).getOrThrow()
        val u = auth.registrarConsentimentos(AuthRules.VERSAO_TERMOS, marketing = false).getOrThrow()
        assertTrue(u.consentimentos.any { it.tipo == "termos:${AuthRules.VERSAO_TERMOS}" && it.concedido })
        assertTrue(u.consentimentos.any { it.tipo == "marketing" && !it.concedido })
        assertEquals(AuthRules.VERSAO_TERMOS, prefs.acceptedTermsVersion)
    }

    @Test
    fun nome_com_menos_de_2_letras_e_recusado() = runTest {
        val auth = repo()
        auth.solicitarOtp("11912345678")
        auth.verificarOtp("11912345678", FakeAuthRepository.CODIGO_OTP_DEMO).getOrThrow()
        assertTrue(auth.atualizarPerfil(" a ").isFailure)
    }

    @Test
    fun onboarding_visto_fica_salvo() {
        assertEquals(false, UiSessionStore(prefs).onboardingDone.value)
        UiSessionStore(prefs).finishOnboarding()
        assertEquals(true, UiSessionStore(prefs).onboardingDone.value)
    }
}
