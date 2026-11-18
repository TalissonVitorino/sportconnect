package br.com.sportconnect.feature.arenaadmin

import br.com.sportconnect.core.domain.usecase.GetArenasNearbyUseCase
import br.com.sportconnect.core.model.StatusArena
import br.com.sportconnect.feature.arenaadmin.common.LoadState
import br.com.sportconnect.feature.arenaadmin.onboarding.OnboardingStep
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** T9 (13/US-012): o cadastro passa a carregar/salvar a arena de verdade e so publica quando completo. */
@OptIn(ExperimentalCoroutinesApi::class)
class ArenaOnboardingViewModelTest {

    @BeforeTest fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())
    @AfterTest fun tearDown() = Dispatchers.resetMain()

    @Test
    fun carrega_os_dados_da_arena_do_gestor_e_marca_as_etapas_ja_completas() = runTest {
        val env = ArenaTestEnv()
        env.loginGestor()
        val vm = env.onboardingVm()
        val s = vm.state.value
        assertEquals(LoadState.Ready, s.load)
        assertEquals("Arena Vila Madalena Sports", s.name)
        assertEquals("Rua Fradique Coutinho", s.logradouro)
        assertEquals("05416-001", s.cep)
        assertEquals("(11) 3031-4455", s.telefone)
        assertTrue("Vestiário" in s.comodidades)
        assertTrue(s.published)
        // Courts (ja tem quadra) e Schedule (arena ja ATIVA) vem completas; Info (CPF nunca persistido,
        // sempre volta vazio) e Bank (a semente nao tem chave Pix) precisam ser preenchidas de novo.
        assertEquals(setOf(OnboardingStep.Courts, OnboardingStep.Schedule), s.done)
    }

    @Test
    fun salvar_a_etapa_info_persiste_endereco_telefone_e_comodidades_na_arena_real() = runTest {
        val env = ArenaTestEnv()
        env.loginGestor()
        val vm = env.onboardingVm()
        vm.open(OnboardingStep.Info)
        vm.setDocument("12345678901")
        vm.setTelefone("11988887777")
        vm.setBairro("Pinheiros")
        vm.saveStep()

        assertTrue(OnboardingStep.Info in vm.state.value.done)
        val salvo = env.arenas.arena(ArenaTestEnv.VM)!!
        assertEquals("Pinheiros", salvo.endereco.bairro)
        assertEquals("11988887777", salvo.telefone)
        // CPF do responsavel nao tem campo na Arena (so o CNPJ dela, que e outro dado) -> nao e sobrescrito.
        assertEquals("12.345.678/0001-90", salvo.documento)
    }

    @Test
    fun publicar_so_libera_depois_das_4_etapas_e_a_arena_volta_a_aparecer_na_busca() = runTest {
        val env = ArenaTestEnv()
        env.loginGestor()
        // Simula uma arena ainda nao publicada (ex.: acabou de ser cadastrada pelo comercial).
        env.arenas.atualizarArena(ArenaTestEnv.VM) { it.copy(status = StatusArena.EM_CADASTRO) }
        val buscar = GetArenasNearbyUseCase(env.arenas, env.analytics)
        assertFalse(buscar().any { it.arena.id == ArenaTestEnv.VM })

        val vm = env.onboardingVm()
        assertFalse(vm.state.value.published)
        assertFalse(vm.state.value.canPublish)

        vm.open(OnboardingStep.Info)
        vm.setDocument("12345678901")
        vm.saveStep()
        vm.open(OnboardingStep.Schedule)
        vm.saveStep()
        vm.open(OnboardingStep.Bank)
        vm.setPixKey("pagamentos@arena.com")
        vm.saveStep()
        // Courts ja estava completa: a arena ja tinha quadra cadastrada na semente.
        assertEquals(OnboardingStep.entries.toSet(), vm.state.value.done)
        assertTrue(vm.state.value.canPublish)

        vm.publish()
        assertTrue(vm.state.value.published)
        assertEquals(StatusArena.ATIVA, env.arenas.arena(ArenaTestEnv.VM)!!.status)
        assertTrue(buscar().any { it.arena.id == ArenaTestEnv.VM })
    }
}
