package br.com.sportconnect.core

import br.com.sportconnect.core.data.fake.DemoData
import br.com.sportconnect.core.data.fake.FakeArenaRepository
import br.com.sportconnect.core.data.fake.FakeAuthRepository
import br.com.sportconnect.core.data.fake.FakeGameRepository
import br.com.sportconnect.core.data.fake.FakePaymentGateway
import br.com.sportconnect.core.data.fake.InMemoryAnalyticsTracker
import br.com.sportconnect.core.data.fake.PixBrCode
import br.com.sportconnect.core.domain.HoldDeadlineCalculator
import br.com.sportconnect.core.domain.PayoutCalculator
import br.com.sportconnect.core.domain.ReservationService
import br.com.sportconnect.core.domain.ReservationStateMachine
import br.com.sportconnect.core.domain.SplitCostUseCase
import br.com.sportconnect.core.domain.WaitlistManager
import br.com.sportconnect.core.domain.repository.AnalyticsEvents
import br.com.sportconnect.core.domain.usecase.ArenaDashboardUseCase
import br.com.sportconnect.core.domain.usecase.CancelGameUseCase
import br.com.sportconnect.core.domain.usecase.CreateGameUseCase
import br.com.sportconnect.core.domain.usecase.GetArenasNearbyUseCase
import br.com.sportconnect.core.domain.usecase.GetAvailableSlotsUseCase
import br.com.sportconnect.core.domain.usecase.GetMyGamesUseCase
import br.com.sportconnect.core.domain.usecase.GetOpenGamesUseCase
import br.com.sportconnect.core.domain.usecase.JoinGameByInviteUseCase
import br.com.sportconnect.core.domain.usecase.NovoJogo
import br.com.sportconnect.core.domain.usecase.PayShareUseCase
import br.com.sportconnect.core.domain.usecase.RegraNegocioException
import br.com.sportconnect.core.model.EtapaPrazo
import br.com.sportconnect.core.model.Money
import br.com.sportconnect.core.model.SlotStatus
import br.com.sportconnect.core.model.Sport
import br.com.sportconnect.core.model.StatusCota
import br.com.sportconnect.core.model.StatusPagamento
import br.com.sportconnect.core.model.StatusReserva
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

class FakeDataIntegrationTest {

    /** Segunda 16/11/2026 15:00 em Sao Paulo. */
    private val inicio = Instant.parse("2026-11-16T18:00:00Z")

    private inner class Ambiente(scope: TestScope) {
        val clock = TestClock(inicio)
        val seed = DemoData(clock)
        val analytics = InMemoryAnalyticsTracker()
        val arenas = FakeArenaRepository(seed)
        val games = FakeGameRepository(seed)
        val auth = FakeAuthRepository(seed, analytics, clock)
        val gateway = FakePaymentGateway(clock, scope.backgroundScope)
        val sm = ReservationStateMachine()
        val service = ReservationService(games, arenas, gateway, analytics, sm, PayoutCalculator(), WaitlistManager(), clock)
        val payShare = PayShareUseCase(games, gateway, auth, analytics, clock)

        init {
            service.start(scope.backgroundScope, intervalo = 1.hours)
            scope.runCurrent()
        }
    }

    @Test
    fun seed_tem_arenas_slots_e_jogos_em_estados_variados() = runTest {
        val env = Ambiente(this)
        assertEquals(6, env.seed.arenas.size)
        assertTrue(env.seed.quadras.size >= 15)
        assertTrue(env.seed.quadras.all { it.precoHora >= Money.reais(120) && it.precoHoraPico <= Money.reais(400) })
        assertEquals(env.seed.quadras.size * 7 * 16, env.seed.slots.size)
        val status = env.seed.jogos.map { it.status }.toSet()
        assertTrue(status.containsAll(listOf(StatusReserva.AGUARDANDO_PAGAMENTOS, StatusReserva.CONFIRMADO, StatusReserva.CONCLUIDO, StatusReserva.EXPIRADO)))
        val racha = env.games.game(DemoData.JOGO_RACHA)!!
        assertEquals(8, racha.cotasPagas)
        assertEquals(12, racha.numeroVagas)
        assertEquals(env.seed.slots[racha.slotId]!!.preco, racha.valorTotal)
        assertEquals(SlotStatus.PRE_RESERVADO, env.arenas.slot(racha.slotId)!!.status)
        assertTrue(racha.reserva.prazoCotas!! > inicio)
    }

    @Test
    fun pagar_cota_pix_confirma_apos_3s_via_webhook_e_atualiza_flow() = runTest {
        val env = Ambiente(this)
        env.auth.entrarComo("u-atleta-10")
        val cobranca = env.payShare(DemoData.JOGO_RACHA).getOrThrow()
        assertTrue(cobranca.pixCopiaECola!!.startsWith("000201"))
        assertEquals(Money(150), cobranca.taxaConveniencia)
        assertEquals(8, env.games.game(DemoData.JOGO_RACHA)!!.cotasPagas)

        advanceTimeBy(3.seconds + 100.milliseconds)
        runCurrent()
        val jogo = env.games.observeGame(DemoData.JOGO_RACHA).first()!!
        assertEquals(9, jogo.cotasPagas)
        assertEquals(StatusPagamento.CONFIRMADO, env.gateway.pagamento(cobranca.pagamentoId)!!.status)
        assertTrue(AnalyticsEvents.COTA_PAGA in env.analytics.nomes())
    }

    @Test
    fun ultimas_cotas_pagas_confirmam_o_jogo_e_reservam_o_slot() = runTest {
        val env = Ambiente(this)
        for (u in listOf("u-atleta-10", "u-atleta-11", "u-atleta-12", "u-atleta-13")) {
            env.auth.entrarComo(u)
            env.payShare(DemoData.JOGO_RACHA).getOrThrow()
        }
        advanceTimeBy(4.seconds)
        runCurrent()
        val jogo = env.games.game(DemoData.JOGO_RACHA)!!
        assertEquals(StatusReserva.CONFIRMADO, jogo.status)
        assertEquals(SlotStatus.RESERVADO, env.arenas.slot(jogo.slotId)!!.status)
        assertTrue(AnalyticsEvents.JOGO_CONFIRMADO in env.analytics.nomes())
    }

    @Test
    fun pagar_duas_vezes_no_mesmo_jogo_e_bloqueado() = runTest {
        val env = Ambiente(this)
        // Organizador ja pagou sua cota no racha.
        val r = env.payShare(DemoData.JOGO_RACHA)
        assertEquals("JA_PAGOU", (r.exceptionOrNull() as RegraNegocioException).codigo)
    }

    @Test
    fun criar_jogo_e_pagar_cota_do_organizador_ativa_pre_reserva() = runTest {
        val env = Ambiente(this)
        val slots = GetAvailableSlotsUseCase(env.arenas, env.clock)
        val amanha = env.seed.dia(1)
        val livre = slots(DemoData.ARENA_TATUAPE, amanha, Sport.FUTEBOL_SOCIETY).first().first()
        val create = CreateGameUseCase(env.games, env.arenas, env.auth, SplitCostUseCase(), HoldDeadlineCalculator(), env.analytics, env.clock)
        val jogo = create(NovoJogo(slotId = livre.slot.id, esporte = Sport.FUTEBOL_SOCIETY, numeroVagas = 14)).getOrThrow()
        assertEquals(StatusReserva.RASCUNHO, jogo.status)
        assertEquals(SlotStatus.EM_CHECKOUT, env.arenas.slot(livre.slot.id)!!.status)
        assertEquals(livre.slot.preco, Money(jogo.reserva.cotas.sumOf { it.valor.cents }))

        env.payShare(jogo.id).getOrThrow()
        advanceTimeBy(4.seconds)
        runCurrent()
        val ativo = env.games.game(jogo.id)!!
        assertEquals(StatusReserva.AGUARDANDO_PAGAMENTOS, ativo.status)
        assertEquals(EtapaPrazo.COLETANDO_COTAS, ativo.etapaPrazo)
        assertEquals(SlotStatus.PRE_RESERVADO, env.arenas.slot(livre.slot.id)!!.status)
        assertTrue(AnalyticsEvents.PRE_RESERVA_ATIVADA in env.analytics.nomes())
        // O slot nao e mais oferecido.
        assertTrue(slots(DemoData.ARENA_TATUAPE, amanha).first().none { it.slot.id == livre.slot.id })
    }

    @Test
    fun criar_jogo_com_vagas_fora_da_rn05_falha() = runTest {
        val env = Ambiente(this)
        val livre = GetAvailableSlotsUseCase(env.arenas, env.clock)(DemoData.ARENA_MOEMA, env.seed.dia(1), Sport.PADEL).first().first()
        val create = CreateGameUseCase(env.games, env.arenas, env.auth, SplitCostUseCase(), HoldDeadlineCalculator(), env.analytics, env.clock)
        val r = create(NovoJogo(slotId = livre.slot.id, esporte = Sport.PADEL, numeroVagas = 6))
        assertEquals("VAGAS_INVALIDAS", (r.exceptionOrNull() as RegraNegocioException).codigo)
    }

    @Test
    fun prazo_vencido_aplica_politica_e_pagamento_tardio_e_estornado() = runTest {
        val env = Ambiente(this)
        val racha = env.games.game(DemoData.JOGO_RACHA)!!
        env.auth.entrarComo("u-atleta-10")
        val cobranca = env.payShare(racha.id).getOrThrow()
        // O job de prazo roda (P vencido) antes de o Pix ser confirmado pelo gateway.
        env.service.processarDeadlines(racha.reserva.prazoCotas!! + 1.minutes)
        assertEquals(EtapaPrazo.JANELA_RESOLUCAO, env.games.game(racha.id)!!.etapaPrazo)
        // Webhook chega depois: pagamento tardio e estornado integralmente (RN-18).
        advanceTimeBy(3.seconds + 100.milliseconds)
        runCurrent()
        val status = env.gateway.pagamento(cobranca.pagamentoId)!!.status
        assertTrue(status == StatusPagamento.ESTORNO_EM_PROCESSAMENTO || status == StatusPagamento.ESTORNADO, "status=$status")
        assertEquals(8, env.games.game(racha.id)!!.cotasPagas)
    }

    @Test
    fun organizador_cancela_e_slot_volta_a_ficar_livre() = runTest {
        val env = Ambiente(this)
        val cancel = CancelGameUseCase(env.service, env.games, env.arenas, env.auth, env.clock)
        val jogo = cancel(DemoData.JOGO_RACHA, "Pouca gente").getOrThrow()
        assertEquals(StatusReserva.CANCELADO, jogo.status)
        assertTrue(jogo.reserva.cotas.none { it.status == StatusCota.PAGA || it.status == StatusCota.PENDENTE })
        assertEquals(SlotStatus.LIVRE, env.arenas.slot(jogo.slotId)!!.status)
    }

    @Test
    fun jogo_concluido_gera_repasse_com_comissao_do_plano() = runTest {
        val env = Ambiente(this)
        val futsal = env.games.game(DemoData.JOGO_FUTSAL_CONFIRMADO)!!
        env.clock.agora = futsal.fim + 1.minutes
        env.service.processarDeadlines()
        assertEquals(StatusReserva.CONCLUIDO, env.games.game(futsal.id)!!.status)
        val repasse = env.arenas.observeRepasses(futsal.arenaId).first().first { it.jogoId == futsal.id }
        assertEquals(Money(futsal.valorTotal.cents * 4 / 100), repasse.comissao) // arena PRO
        assertEquals(futsal.valorTotal - repasse.comissao, repasse.valorLiquido)
    }

    @Test
    fun convite_por_codigo_jogos_abertos_meus_jogos_e_busca() = runTest {
        val env = Ambiente(this)
        val join = JoinGameByInviteUseCase(env.games, env.analytics, env.clock)
        assertEquals(DemoData.JOGO_RACHA, join("racha8").getOrThrow().id)
        assertEquals(DemoData.JOGO_VOLEI, join("https://sportconnect.app/j/VOLEI5").getOrThrow().id)
        assertTrue(join("NAOEXISTE").isFailure)

        val abertos = GetOpenGamesUseCase(env.games, env.clock)().first()
        assertEquals(3, abertos.size)
        assertTrue(abertos.all { it.vagasRestantes > 0 })

        val meus = GetMyGamesUseCase(env.games, env.auth)().first()
        assertTrue(meus.proximos.any { it.id == DemoData.JOGO_RACHA })
        assertTrue(meus.historico.any { it.id == DemoData.JOGO_BEACH_CONCLUIDO })

        val proximas = GetArenasNearbyUseCase(env.arenas, env.analytics)(esporte = Sport.BEACH_TENNIS)
        assertEquals(2, proximas.size)
        assertTrue(proximas.zipWithNext().all { (a, b) -> a.distanciaKm <= b.distanciaKm })
    }

    @Test
    fun painel_da_arena_mostra_agenda_ocupacao_e_repasses() = runTest {
        val env = Ambiente(this)
        val painel = ArenaDashboardUseCase(env.arenas, env.games, env.analytics)(DemoData.ARENA_VILA_MADALENA, env.seed.dia(1)).first()
        assertEquals(3 * 16, painel.agenda.size)
        assertTrue(painel.ocupacaoPct in 1..100)
        assertTrue(painel.agenda.any { it.jogo?.id == DemoData.JOGO_FUTSAL_CONFIRMADO })
        assertTrue(painel.repasses.isNotEmpty())
        assertTrue(painel.repasseLiquidado > Money.ZERO)
    }

    @Test
    fun pix_copia_e_cola_tem_crc_valido() {
        val code = PixBrCode.gerar(Money(2_150), "SCTESTE00001")
        assertTrue(code.contains("br.gov.bcb.pix"))
        assertTrue(code.contains("540521.50"))
        assertEquals(PixBrCode.crc16(code.dropLast(4)), code.takeLast(4))
    }

}
