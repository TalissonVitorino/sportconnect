package br.com.sportconnect.feature.arenaadmin.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.sportconnect.core.domain.PayoutCalculator
import br.com.sportconnect.core.domain.repository.AnalyticsTracker
import br.com.sportconnect.core.domain.repository.ArenaRepository
import br.com.sportconnect.core.domain.repository.AuthRepository
import br.com.sportconnect.core.domain.repository.GameRepository
import br.com.sportconnect.core.domain.usecase.ArenaDashboardUseCase
import br.com.sportconnect.core.domain.usecase.PainelArena
import br.com.sportconnect.core.model.Jogo
import br.com.sportconnect.core.model.Money
import br.com.sportconnect.core.model.Quadra
import br.com.sportconnect.core.model.Sport
import br.com.sportconnect.core.model.StatusRepasse
import br.com.sportconnect.core.model.StatusReserva
import br.com.sportconnect.feature.arenaadmin.common.AgendaCellStatus
import br.com.sportconnect.feature.arenaadmin.common.AgendaCellUi
import br.com.sportconnect.feature.arenaadmin.common.AgendaMapper
import br.com.sportconnect.feature.arenaadmin.common.ArenaAgenda
import br.com.sportconnect.feature.arenaadmin.common.ArenaBlockController
import br.com.sportconnect.feature.arenaadmin.common.ArenaFormat
import br.com.sportconnect.feature.arenaadmin.common.ArenaFormat.local
import br.com.sportconnect.feature.arenaadmin.common.ArenaFormat.localDate
import br.com.sportconnect.feature.arenaadmin.common.ArenaMessage
import br.com.sportconnect.feature.arenaadmin.common.BlockReason
import br.com.sportconnect.feature.arenaadmin.common.BlockSheetUi
import br.com.sportconnect.feature.arenaadmin.common.BookingDetailUi
import br.com.sportconnect.feature.arenaadmin.common.BookingMapper
import br.com.sportconnect.feature.arenaadmin.common.LoadState
import br.com.sportconnect.feature.arenaadmin.common.isManager
import br.com.sportconnect.feature.arenaadmin.common.observeArenaId
import br.com.sportconnect.feature.arenaadmin.courts.ArenaCourtStore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import kotlin.time.Clock
import kotlin.time.Instant

/** Linha "Agora" por quadra: o horario em curso ou o proximo ocupado do dia ([cell] null = livre). */
data class NowRowUi(val courtId: String, val courtName: String, val cell: AgendaCellUi?)

data class HoldUi(
    val jogoId: String,
    val slotId: String,
    val courtName: String,
    val sport: Sport,
    val dayLabel: String,
    val isToday: Boolean,
    val rangeLabel: String,
    val organizer: String?,
    val paid: Int,
    val total: Int,
    val deadline: Instant?,
    val deadlineLabel: String?,
)

/** Alerta "Quadra X sem horarios a partir de dd/MM/aaaa" (`arena_alert_no_schedule`). */
data class AlertUi(val id: String, val courtId: String, val courtName: String, val fromDate: String, val blocking: Boolean = false)

data class NextPayoutUi(val amount: Money, val dateLabel: String, val projected: Boolean)

data class DaySummaryUi(
    val confirmed: Int = 0,
    val holds: Int = 0,
    val blocked: Int = 0,
    val free: Int = 0,
    val occupancyPct: Int = 0,
    val revenue: Money = Money.ZERO,
)

data class ArenaDashboardUiState(
    val load: LoadState = LoadState.Loading,
    val arenaName: String = "",
    val date: LocalDate? = null,
    val isToday: Boolean = true,
    val dateLabel: String = "",
    /** Minutos desde 00:00 do "agora" (linha vermelha da grade). */
    val nowMinutes: Int = 0,
    val agenda: ArenaAgenda = ArenaAgenda.EMPTY,
    val nowRows: List<NowRowUi> = emptyList(),
    val holds: List<HoldUi> = emptyList(),
    val alerts: List<AlertUi> = emptyList(),
    val summary: DaySummaryUi = DaySummaryUi(),
    val nextPayout: NextPayoutUi? = null,
    /** Gestor ve financeiro; operador nao. */
    val showFinancials: Boolean = true,
    val selectedSlotId: String? = null,
    val selected: BookingDetailUi? = null,
    val block: BlockSheetUi? = null,
    val message: ArenaMessage? = null,
) {
    val isEmpty: Boolean get() = load == LoadState.Ready && agenda.courts.all { c -> c.cells.none { it.isBooking } }
}

/**
 * Painel da arena (10 §2.17): agenda do dia em tempo real, "Agora" por quadra, pre-reservas, alertas,
 * resumo e proximo repasse. Bloqueio via [ArenaBlockController].
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ArenaDashboardViewModel(
    private val dashboard: ArenaDashboardUseCase,
    private val arenas: ArenaRepository,
    private val games: GameRepository,
    private val auth: AuthRepository,
    private val payout: PayoutCalculator,
    private val courtStore: ArenaCourtStore,
    private val analytics: AnalyticsTracker?,
    private val clock: Clock,
) : ViewModel() {

    private val _state = MutableStateFlow(ArenaDashboardUiState())
    val state: StateFlow<ArenaDashboardUiState> = _state.asStateFlow()

    private val now = MutableStateFlow(clock.now())
    private val retry = MutableStateFlow(0)
    private val selectedSlot = MutableStateFlow<String?>(null)
    /** Dia exibido na grade (expanded); null = hoje. */
    private val date = MutableStateFlow<LocalDate?>(null)
    private var lastPainel: PainelArena? = null

    private val blocker = ArenaBlockController(arenas, analytics, viewModelScope) { msg -> _state.update { it.copy(message = msg) } }

    init {
        blocker.sheet.onEach { s -> _state.update { it.copy(block = s) } }.launchIn(viewModelScope)
        retry.flatMapLatest {
            auth.observeArenaId().flatMapLatest { arenaId ->
                if (arenaId == null) flowOf<ArenaDashboardUiState?>(null)
                else load(arenaId)
            }.catch { emit(_state.value.copy(load = LoadState.Error)) }
        }
            .onEach { s ->
                _state.update { old ->
                    s?.copy(block = old.block, message = old.message) ?: old.copy(load = LoadState.NoArena)
                }
            }
            .launchIn(viewModelScope)
    }

    private class Ctx(val hoje: LocalDate, val quadras: List<Quadra>, val missing: Map<String, LocalDate>)

    private fun load(arenaId: String) = flow {
        val hoje = clock.now().localDate()
        val quadras = arenas.quadras(arenaId).filter { it.ativa }.sortedBy { it.nome }
        emit(Ctx(hoje, quadras, computeMissingSchedule(arenaId, hoje)))
    }.flatMapLatest { ctx ->
        val hoje = ctx.hoje
        combine(
            date.flatMapLatest { d -> dashboard(arenaId, d ?: hoje) },
            games.observeGames(),
            courtStore.publishedUntil,
            now,
            selectedSlot,
        ) { painel, jogos, published, agora, sel ->
            lastPainel = painel
            build(painel, ctx.quadras, jogos.filter { it.arenaId == arenaId }, ctx.missing, published, agora, sel, hoje)
        }
    }

    /** Primeiro dia (em hoje..hoje+7) sem nenhum horario ofertado, por quadra. */
    private suspend fun computeMissingSchedule(arenaId: String, hoje: LocalDate): Map<String, LocalDate> {
        val quadras = arenas.quadras(arenaId).filter { it.ativa }
        val result = mutableMapOf<String, LocalDate>()
        for (d in 0..HORIZON_DAYS) {
            val date = hoje.plus(DatePeriod(days = d))
            val withSlots = arenas.observeSlots(arenaId, date).first().map { it.quadraId }.toSet()
            quadras.filter { it.id !in withSlots && it.id !in result }.forEach { result[it.id] = date }
        }
        return result
    }

    private fun build(
        painel: PainelArena,
        quadras: List<Quadra>,
        jogos: List<Jogo>,
        missing: Map<String, LocalDate>,
        published: Map<String, LocalDate>,
        agora: Instant,
        sel: String?,
        hoje: LocalDate,
    ): ArenaDashboardUiState {
        val agenda = AgendaMapper.agenda(quadras, painel.agenda, agora)
        blocker.updateAgenda(agenda)
        val local = agora.local()
        val nowRows = if (painel.data != hoje) emptyList() else agenda.courts.map { court ->
            val current = court.cells.firstOrNull { it.hour == local.hour && it.isBooking }
            val next = court.cells.firstOrNull { it.hour > local.hour && it.isBooking }
            NowRowUi(court.courtId, court.name, current ?: next)
        }
        val holds = jogos.filter { it.status == StatusReserva.AGUARDANDO_PAGAMENTOS && it.fim > agora }
            .sortedBy { it.inicio }
            .take(MAX_HOLDS)
            .map { j ->
                val d = j.inicio.localDate()
                HoldUi(
                    jogoId = j.id, slotId = j.slotId, courtName = j.quadraNome, sport = j.esporte,
                    dayLabel = ArenaFormat.dayLabel(d), isToday = d == hoje,
                    rangeLabel = ArenaFormat.range(j.inicio, j.fim), organizer = ArenaFormat.shortName(j.organizadorNome),
                    paid = j.cotasPagas, total = j.numeroVagas, deadline = j.reserva.prazoHold,
                    deadlineLabel = j.reserva.prazoHold?.let { p ->
                        val pd = p.localDate()
                        if (pd == hoje) ArenaFormat.hour(p) else "${ArenaFormat.dayLabel(pd)}, ${ArenaFormat.hour(p)}"
                    },
                )
            }
        val alerts = missing.mapNotNull { (courtId, date) ->
            val pub = published[courtId]
            if (pub != null && pub >= date) return@mapNotNull null
            val name = quadras.firstOrNull { it.id == courtId }?.nome ?: return@mapNotNull null
            AlertUi("no_schedule_$courtId", courtId, name, ArenaFormat.fullDate(date))
        }
        val selected = sel?.let { id -> painel.agenda.firstOrNull { it.slot.id == id } }?.let { item ->
            BookingMapper.detail(item.slot, item.quadra, item.jogo, painel.arena.plano, payout, agora)
        }
        return ArenaDashboardUiState(
            load = LoadState.Ready,
            arenaName = painel.arena.nome,
            date = painel.data,
            isToday = painel.data == hoje,
            dateLabel = ArenaFormat.dayLabel(painel.data),
            nowMinutes = local.hour * 60 + local.minute,
            agenda = agenda,
            nowRows = nowRows,
            holds = holds,
            alerts = alerts,
            summary = DaySummaryUi(
                confirmed = painel.confirmados,
                holds = painel.preReservas,
                blocked = painel.bloqueados,
                free = painel.livres,
                occupancyPct = painel.ocupacaoPct,
                revenue = painel.receitaPrevistaDia,
            ),
            nextPayout = nextPayout(painel, jogos, hoje),
            showFinancials = auth.isManager(),
            selectedSlotId = selected?.slotId,
            selected = selected,
        )
    }

    /** Proximo repasse: o agendado mais proximo ou, sem agendados, a projecao dos jogos confirmados (RN-30). */
    private fun nextPayout(painel: PainelArena, jogos: List<Jogo>, hoje: LocalDate): NextPayoutUi? {
        val withRepasse = painel.repasses.map { it.jogoId }.toSet()
        val scheduled = painel.repasses.filter { it.status == StatusRepasse.AGENDADO && it.dataPrevista >= hoje }
            .map { it.dataPrevista to it.valorLiquido }
        val projected = jogos.filter {
            (it.status == StatusReserva.CONFIRMADO || it.status == StatusReserva.EM_ANDAMENTO) && it.id !in withRepasse
        }.map { j ->
            payout.proximoDiaUtil(j.fim.localDate()) to payout.calcular(j.valorTotal, painel.arena.plano).liquido
        }
        val all = scheduled.map { Triple(it.first, it.second, false) } + projected.map { Triple(it.first, it.second, true) }
        val date = all.minOfOrNull { it.first } ?: return null
        val sameDay = all.filter { it.first == date }
        return NextPayoutUi(
            amount = Money(sameDay.sumOf { it.second.cents }),
            dateLabel = ArenaFormat.dayLabel(date),
            projected = sameDay.all { it.third },
        )
    }

    // ------------------------------------------------------------------ acoes

    fun refreshNow() { now.value = clock.now() }
    fun retry() {
        _state.update { it.copy(load = LoadState.Loading) }
        retry.update { it + 1 }
    }

    /** Seleciona uma celula da grade (expanded): reserva abre o painel lateral; livre abre o bloqueio. */
    fun onCellClick(cell: AgendaCellUi) {
        when {
            cell.slotId == null -> Unit
            cell.status == AgendaCellStatus.Free -> blocker.open(slotId = cell.slotId)
            else -> selectedSlot.value = cell.slotId
        }
    }

    fun select(slotId: String?) { selectedSlot.value = slotId }

    fun previousDay() = shiftDay(-1)
    fun nextDay() = shiftDay(1)
    fun today() { date.value = null; selectedSlot.value = null }
    private fun shiftDay(delta: Int) {
        val base = date.value ?: clock.now().localDate()
        date.value = base.plus(DatePeriod(days = delta))
        selectedSlot.value = null
    }

    fun openBlock(slotId: String? = null, courtId: String? = null) = blocker.open(slotId, courtId)
    fun selectBlockCourt(courtId: String) = blocker.selectCourt(courtId)
    fun selectBlockSlot(slotId: String) = blocker.selectSlot(slotId)
    fun selectBlockReason(reason: BlockReason) = blocker.selectReason(reason)
    fun dismissBlock() = blocker.dismiss()
    fun confirmBlock(reasonLabel: String) = blocker.confirm(reasonLabel)
    fun undoBlock(slotId: String) = blocker.unblock(slotId)
    fun unblock(slotId: String) = blocker.unblock(slotId, announce = true)
    fun consumeMessage() = _state.update { it.copy(message = null) }

    /** Para testes/screenshots: ultimo painel bruto recebido. */
    internal fun painel(): PainelArena? = lastPainel

    companion object {
        const val HORIZON_DAYS = 7
        const val MAX_HOLDS = 5
    }
}
