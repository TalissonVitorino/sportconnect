package br.com.sportconnect.feature

import kotlinx.coroutines.launch
import br.com.sportconnect.core.TestClock
import br.com.sportconnect.core.data.fake.DemoData
import br.com.sportconnect.core.data.fake.FakeArenaRepository
import br.com.sportconnect.core.data.fake.FakeAuthRepository
import br.com.sportconnect.core.data.fake.FakeGameRepository
import br.com.sportconnect.core.data.fake.FakePaymentGateway
import br.com.sportconnect.core.data.fake.InMemoryAnalyticsTracker
import br.com.sportconnect.core.domain.HoldDeadlineCalculator
import br.com.sportconnect.core.domain.PayoutCalculator
import br.com.sportconnect.core.domain.ReservationService
import br.com.sportconnect.core.domain.ReservationStateMachine
import br.com.sportconnect.core.domain.SplitCostUseCase
import br.com.sportconnect.core.domain.WaitlistManager
import br.com.sportconnect.core.domain.usecase.AvisosLidosStore
import br.com.sportconnect.core.domain.usecase.CancelGameUseCase
import br.com.sportconnect.core.domain.usecase.CreateGameUseCase
import br.com.sportconnect.core.domain.usecase.GetMyGamesUseCase
import br.com.sportconnect.core.domain.usecase.GetNotificationsUseCase
import br.com.sportconnect.core.domain.usecase.JoinWaitlistUseCase
import br.com.sportconnect.core.domain.usecase.LeaveGameUseCase
import br.com.sportconnect.core.domain.usecase.LeaveWaitlistUseCase
import br.com.sportconnect.core.domain.usecase.ObserveGameWithWaitlistUseCase
import br.com.sportconnect.core.domain.usecase.ToggleSpotOfferUseCase
import br.com.sportconnect.core.model.GarantiaCobertura
import br.com.sportconnect.core.model.Money
import br.com.sportconnect.core.model.PoliticaNaoPagamento
import br.com.sportconnect.core.model.SlotStatus
import br.com.sportconnect.core.model.Sport
import br.com.sportconnect.core.model.StatusReserva
import br.com.sportconnect.feature.common.UiSessionStore
import br.com.sportconnect.feature.creategame.CreateGameEvent
import br.com.sportconnect.feature.creategame.CreateGameViewModel
import br.com.sportconnect.feature.creategame.CreateStep
import br.com.sportconnect.feature.creategame.RuleOption
import br.com.sportconnect.feature.creategame.SpotsError
import br.com.sportconnect.feature.game.GameDetailEvent
import br.com.sportconnect.feature.game.GameDetailUiState
import br.com.sportconnect.feature.game.GameDetailViewModel
import br.com.sportconnect.feature.home.HomeRole
import br.com.sportconnect.feature.home.HomeUiState
import br.com.sportconnect.feature.home.HomeViewModel
import br.com.sportconnect.navigation.Route
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class FeatureViewModelsTest {

    /** Segunda 16/11/2026 15:00 em Sao Paulo. */
    private val inicio = Instant.parse("2026-11-16T18:00:00Z")

    private inner class Env(scope: TestScope) {
        val clock = TestClock(inicio)
        val seed = DemoData(clock)
        val analytics = InMemoryAnalyticsTracker()
        val arenas = FakeArenaRepository(seed)
        val games = FakeGameRepository(seed)
        val auth = FakeAuthRepository(seed, analytics, clock)
        val gateway = FakePaymentGateway(clock, scope.backgroundScope, confirmarAutomaticamente = false)
        val service = ReservationService(games, arenas, gateway, analytics, ReservationStateMachine(), PayoutCalculator(), WaitlistManager(), clock)
        val session = UiSessionStore()
        val split = SplitCostUseCase()
        val hold = HoldDeadlineCalculator()

        fun home() = HomeViewModel(GetMyGamesUseCase(games, auth), GetNotificationsUseCase(games, auth, AvisosLidosStore()), auth, analytics, clock)

        fun detail(id: String) = GameDetailViewModel(
            Route.GameDetail(id), ObserveGameWithWaitlistUseCase(games), auth,
            CancelGameUseCase(service, games, arenas, auth, clock), LeaveGameUseCase(service, games, auth, clock),
            analytics, session, clock, social, ToggleSpotOfferUseCase(games, auth, WaitlistManager(), clock),
            joinWaitlistUseCase = JoinWaitlistUseCase(games, auth, WaitlistManager(), analytics, clock),
            leaveWaitlistUseCase = LeaveWaitlistUseCase(games, auth, WaitlistManager()),
        )

        val social = br.com.sportconnect.core.data.local.SocialStore()

        fun create(slotId: String?) = CreateGameViewModel(
            Route.GameCreate(slotId), arenas, split, hold,
            CreateGameUseCase(games, arenas, auth, split, hold, analytics, clock), analytics, clock,
        )

        /** Primeiro slot livre de [quadraId] com inicio entre [min] e [max] a partir de agora. */
        fun freeSlot(quadraId: String, minHours: Int, maxHours: Int = 24 * 7) = seed.slots.values.first {
            it.quadraId == quadraId && it.status == SlotStatus.LIVRE &&
                it.inicio >= clock.agora + minHours.hours && it.inicio <= clock.agora + maxHours.hours
        }
    }

    private fun TestScope.setUp(): Env {
        Dispatchers.setMain(UnconfinedTestDispatcher(testScheduler))
        return Env(this)
    }

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    // ------------------------------------------------------------------ Home

    @Test
    fun home_mostra_saudacao_e_jogos_que_organizo_antes_dos_confirmados() = runTest {
        val env = setUp()
        val vm = env.home()
        runCurrent()
        val s = assertIs<HomeUiState.Content>(vm.uiState.value)
        assertEquals("Rafael", s.firstName)
        val ids = s.upcoming.map { it.jogo.id }
        assertTrue(DemoData.JOGO_RACHA in ids, "racha deve estar em proximos")
        assertEquals(HomeRole.Organizer, s.upcoming.first { it.jogo.id == DemoData.JOGO_RACHA }.role)
        // Pre-reservas (prazo mais proximo) vem antes dos confirmados.
        val firstConfirmed = s.upcoming.indexOfFirst { it.jogo.status == StatusReserva.CONFIRMADO }
        val lastAwaiting = s.upcoming.indexOfLast { it.jogo.status == StatusReserva.AGUARDANDO_PAGAMENTOS }
        assertTrue(firstConfirmed == -1 || lastAwaiting < firstConfirmed)
        assertTrue(s.pending.isEmpty())
    }

    @Test
    fun home_vazia_para_quem_nao_tem_jogos_e_rascunho_vira_falta_pagar() = runTest {
        val env = setUp()
        env.auth.entrarComo(DemoData.ID_GESTOR)
        val vm = env.home()
        runCurrent()
        assertIs<HomeUiState.Empty>(vm.uiState.value)

        // Organizador cria um rascunho: a cota dele aparece em "Falta pagar".
        env.auth.entrarComo(DemoData.ID_ORGANIZADOR)
        val slot = env.freeSlot("q-vm-futsal", minHours = 24)
        val create = env.create(slot.id)
        runCurrent()
        create.next(); create.next(); create.next(); create.next()
        runCurrent()
        val s = assertIs<HomeUiState.Content>(vm.uiState.value)
        assertEquals(1, s.pending.size)
        assertEquals(StatusReserva.RASCUNHO, s.pending.single().jogo.status)
    }

    // ------------------------------------------------------------------ Criar jogo

    @Test
    fun criar_jogo_rateia_valida_vagas_e_cria_rascunho_com_a_regra_escolhida() = runTest {
        val env = setUp()
        val slot = env.freeSlot("q-vm-society1", minHours = 24)
        val vm = env.create(slot.id)
        runCurrent()
        var s = vm.uiState.value
        assertEquals(Sport.FUTEBOL_SOCIETY, s.sport)
        assertEquals(14, s.spots) // vagas padrao do society, capacidade da quadra = 14
        assertEquals(14, s.maxSpots)
        assertTrue(s.viable.values.all { it }, "longe do jogo todas as regras cabem")
        assertNotNull(s.deadline)

        vm.next() // -> vagas
        vm.setSpots(12)
        s = vm.uiState.value
        assertEquals(CreateStep.Split, s.step)
        assertEquals(Money(slot.preco.cents / 12), s.rateio!!.cotaConvidado)
        assertEquals(slot.preco, s.rateio!!.cotas.fold(Money.ZERO) { a, b -> a + b }, "soma das cotas = V")

        vm.setSpots(15) // acima da capacidade
        assertEquals(SpotsError.Range, vm.uiState.value.spotsError)
        assertFalse(vm.uiState.value.canContinue)
        vm.setSpots(12)

        vm.next() // -> regra
        vm.selectRule(RuleOption.Resplit)
        vm.setCapPct(40)
        assertEquals(CreateStep.Rule, vm.uiState.value.step)
        vm.next() // -> revisao
        assertEquals(CreateStep.Review, vm.uiState.value.step)
        assertEquals(s.rateio!!.cotaOrganizador + s.rateio!!.taxaConveniencia, vm.uiState.value.payNow)

        vm.next() // "Pagar e pre-reservar"
        runCurrent()
        val ev = assertIs<CreateGameEvent.GoToPay>(vm.events.first())
        val jogo = env.games.game(ev.gameId)!!
        assertEquals(StatusReserva.RASCUNHO, jogo.status)
        assertEquals(PoliticaNaoPagamento.RERATEAR_ENTRE_PAGANTES, jogo.politica)
        assertEquals(GarantiaCobertura.NAO_SE_APLICA, jogo.garantia)
        assertEquals(40, jogo.tetoComplementoPct)
        assertEquals(12, jogo.numeroVagas)
    }

    @Test
    fun criar_jogo_perto_do_horario_bloqueia_as_regras_de_rateio() = runTest {
        val env = setUp()
        // 15h agora; slot ate 2h a frente: nao cabe janela de coleta (RN-12).
        val slot = env.seed.slots.values.first {
            it.status == SlotStatus.LIVRE && it.inicio > env.clock.agora && it.inicio <= env.clock.agora + 2.hours
        }
        val vm = env.create(slot.id)
        runCurrent()
        vm.next(); vm.next()
        val s = vm.uiState.value
        assertEquals(CreateStep.Rule, s.step)
        assertTrue(s.viable.values.none { it })
        assertFalse(s.canContinue)
    }

    // ------------------------------------------------------------------ Detalhe do jogo

    @Test
    fun detalhe_do_racha_mostra_rateio_em_tempo_real_e_lista_ordenada() = runTest {
        val env = setUp()
        val vm = env.detail(DemoData.JOGO_RACHA)
        runCurrent()
        var s = assertIs<GameDetailUiState.Content>(vm.uiState.value)
        assertTrue(s.isOrganizer)
        assertEquals(8, s.counts.paid)
        assertEquals(12, s.counts.total)
        assertEquals(4, s.pendingCount)
        assertTrue(s.participants.first().isSelf, "voce vem primeiro")
        assertTrue(s.participants.takeLast(4).all { it.openSpot }, "vagas abertas no fim")
        assertFalse(s.celebrate)

        // Tempo real: uma cota paga por outro caminho atualiza o estado.
        env.games.update(DemoData.JOGO_RACHA) { j ->
            val cotas = j.reserva.cotas.toMutableList()
            val i = cotas.indexOfFirst { it.status == br.com.sportconnect.core.model.StatusCota.PENDENTE }
            cotas[i] = cotas[i].copy(status = br.com.sportconnect.core.model.StatusCota.PAGA, atletaId = "u-atleta-12", atletaNome = "Nicolas Cardoso", pagaEm = env.clock.agora)
            j.copy(reserva = j.reserva.copy(cotas = cotas))
        }
        runCurrent()
        s = assertIs(vm.uiState.value)
        assertEquals(9, s.counts.paid)
    }

    @Test
    fun fechou_aparece_uma_unica_vez_por_jogo() = runTest {
        val env = setUp()
        val first = env.detail(DemoData.JOGO_FUTSAL_CONFIRMADO)
        runCurrent()
        assertTrue(assertIs<GameDetailUiState.Content>(first.uiState.value).celebrate)
        val second = env.detail(DemoData.JOGO_FUTSAL_CONFIRMADO)
        runCurrent()
        assertFalse(assertIs<GameDetailUiState.Content>(second.uiState.value).celebrate)
    }

    @Test
    fun lembrar_respeita_limite_e_cancelar_encerra_o_jogo() = runTest {
        val env = setUp()
        val vm = env.detail(DemoData.JOGO_RACHA)
        runCurrent()
        val s = assertIs<GameDetailUiState.Content>(vm.uiState.value)
        val alvo = s.participants.first { !it.isSelf }
        vm.remindOne(alvo)
        assertIs<GameDetailEvent.RemindOne>(vm.events.first())
        vm.remindOne(alvo)
        assertIs<GameDetailEvent.RemindLimited>(vm.events.first())

        vm.cancel()
        runCurrent()
        assertIs<GameDetailEvent.Cancelled>(vm.events.first())
        assertEquals(StatusReserva.CANCELADO, assertIs<GameDetailUiState.Content>(vm.uiState.value).jogo.status)
    }

    // ------------------------------------------------------------------ E14/E15

    @Test
    fun jogo_lotado_oferece_lista_de_espera_e_mostra_a_posicao() = runTest {
        val env = setUp()
        val vm = env.detail(DemoData.JOGO_PADEL_LOTADO)
        runCurrent()
        var s = assertIs<GameDetailUiState.Content>(vm.uiState.value)
        assertTrue(s.canJoinWaitlist)
        assertEquals(null, s.waitlistPosition)
        vm.joinWaitlist()
        runCurrent()
        s = assertIs(vm.uiState.value)
        assertEquals(1, s.waitlistPosition)
        assertFalse(s.canJoinWaitlist)
        vm.leaveWaitlist()
        runCurrent()
        s = assertIs(vm.uiState.value)
        assertEquals(null, s.waitlistPosition)
        assertTrue(s.canJoinWaitlist)
    }

    @Test
    fun jogo_concluido_pede_avaliacao_uma_vez() = runTest {
        val env = setUp()
        val vm = env.detail(DemoData.JOGO_BEACH_CONCLUIDO)
        runCurrent()
        var s = assertIs<GameDetailUiState.Content>(vm.uiState.value)
        assertTrue(s.canRate)
        assertFalse(s.canJoinWaitlist)
        vm.rate(br.com.sportconnect.core.data.local.GameRating(emptyMap(), 4, ""))
        runCurrent()
        s = assertIs(vm.uiState.value)
        assertFalse(s.canRate)
        assertFalse(assertIs<GameDetailUiState.Content>(env.detail(DemoData.JOGO_RACHA).also { runCurrent() }.uiState.value).canRate)
    }

    // ------------------------------------------------------------------ T3 jogos com vaga

    @Test
    fun jogos_com_vaga_mostram_so_os_de_outras_pessoas_e_filtram_por_esporte() = runTest {
        val env = setUp()
        val vm = br.com.sportconnect.feature.common.OpenGamesViewModel(
            br.com.sportconnect.core.domain.usecase.GetOpenGamesUseCase(env.games, env.clock), env.auth, env.clock,
        )
        backgroundScope.launch { vm.games.collect { } }
        runCurrent()
        val uid = env.auth.currentUser.value!!.id
        val todos = vm.games.value
        assertTrue(todos.isNotEmpty())
        assertTrue(todos.none { it.organizadorId == uid || it.participa(uid) })
        assertTrue(todos.all { it.visibilidade == br.com.sportconnect.core.model.Visibilidade.VAGAS_ABERTAS && it.vagasRestantes > 0 })
        vm.setSport(Sport.BASQUETE)
        runCurrent()
        assertTrue(vm.games.value.isNotEmpty() && vm.games.value.all { it.esporte == Sport.BASQUETE })
    }

    @Test
    fun criar_jogo_com_vagas_abertas_fica_visivel_para_avulsos() = runTest {
        val env = setUp()
        val vm = env.create(env.freeSlot("q-vm-society1", minHours = 24).id)
        runCurrent()
        vm.next(); vm.next(); vm.next()
        vm.setOpenSpots(true)
        vm.next()
        runCurrent()
        val ev = assertIs<CreateGameEvent.GoToPay>(vm.events.first())
        assertEquals(br.com.sportconnect.core.model.Visibilidade.VAGAS_ABERTAS, env.games.game(ev.gameId)!!.visibilidade)
    }
}
