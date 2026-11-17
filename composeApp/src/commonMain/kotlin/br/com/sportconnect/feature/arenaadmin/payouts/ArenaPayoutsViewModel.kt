package br.com.sportconnect.feature.arenaadmin.payouts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.sportconnect.core.domain.repository.AnalyticsEvents
import br.com.sportconnect.core.domain.repository.AnalyticsTracker
import br.com.sportconnect.core.domain.repository.ArenaRepository
import br.com.sportconnect.core.domain.repository.AuthRepository
import br.com.sportconnect.core.domain.repository.GameRepository
import br.com.sportconnect.core.domain.PayoutCalculator
import br.com.sportconnect.core.model.Plano
import br.com.sportconnect.core.model.StatusReserva
import br.com.sportconnect.core.model.Money
import br.com.sportconnect.core.model.Repasse
import br.com.sportconnect.core.model.StatusRepasse
import br.com.sportconnect.feature.arenaadmin.common.ArenaFormat
import br.com.sportconnect.feature.arenaadmin.common.ArenaFormat.localDate
import br.com.sportconnect.feature.arenaadmin.common.LoadState
import br.com.sportconnect.feature.arenaadmin.common.observeArenaId
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlin.time.Clock

/** Filtro de periodo (`payout_period_filter`): ultimos N dias pela data prevista. */
enum class PayoutPeriod(val days: Int?) { Last7(7), Last30(30), Last90(90), All(null) }

data class PayoutItemUi(
    val id: String,
    val jogoId: String,
    val date: LocalDate,
    val dateLabel: String,
    val description: String,
    val status: StatusRepasse,
    val gross: Money,
    val fee: Money,
    val net: Money,
    /** "22/11/2026" previsto ou data de pagamento. */
    val expectedLabel: String,
    val paidLabel: String?,
    /** Previsao a partir de jogo confirmado que ainda nao gerou repasse (RN-30: D+1 util apos o jogo). */
    val projected: Boolean = false,
)

data class ArenaPayoutsUiState(
    val load: LoadState = LoadState.Loading,
    val period: PayoutPeriod = PayoutPeriod.Last30,
    val items: List<PayoutItemUi> = emptyList(),
    val totalScheduled: Money = Money.ZERO,
    val totalPaid: Money = Money.ZERO,
    val next: PayoutItemUi? = null,
    val nextAmount: Money = Money.ZERO,
    val heldCount: Int = 0,
    val bankDocument: String = "",
    val selected: PayoutItemUi? = null,
    /** CSV pronto para a UI copiar/baixar; consumido com [ArenaPayoutsViewModel.exportHandled]. */
    val exportCsv: String? = null,
)

/** Repasses (10 §2.21): previsto/pago, filtro de periodo, lista e exportacao do extrato em CSV. */
@OptIn(ExperimentalCoroutinesApi::class)
class ArenaPayoutsViewModel(
    private val arenas: ArenaRepository,
    private val games: GameRepository,
    private val auth: AuthRepository,
    private val payout: PayoutCalculator,
    private val analytics: AnalyticsTracker?,
    private val clock: Clock,
) : ViewModel() {

    private val _state = MutableStateFlow(ArenaPayoutsUiState())
    val state: StateFlow<ArenaPayoutsUiState> = _state.asStateFlow()

    private val period = MutableStateFlow(PayoutPeriod.Last30)
    private val selectedId = MutableStateFlow<String?>(null)
    private val retry = MutableStateFlow(0)

    init {
        retry.flatMapLatest {
            auth.observeArenaId().flatMapLatest { arenaId ->
                if (arenaId == null) flowOf<ArenaPayoutsUiState?>(null)
                else flow { emit(arenas.arena(arenaId)) }.flatMapLatest { arena ->
                    combine(arenas.observeRepasses(arenaId), games.observeGames(), period, selectedId) { repasses, jogos, p, sel ->
                        val comRepasse = repasses.map { it.jogoId }.toSet()
                        val projetados = jogos.filter {
                            it.arenaId == arenaId && it.id !in comRepasse &&
                                (it.status == StatusReserva.CONFIRMADO || it.status == StatusReserva.EM_ANDAMENTO)
                        }.map { j ->
                            payout.gerarRepasse(
                                "proj-${j.id}", arenaId, j.id, j.valorTotal, arena?.plano ?: Plano.BASICO,
                                j.fim.localDate(), "${j.esporte.displayName} · ${j.quadraNome}",
                            )
                        }
                        build(repasses, projetados, p, sel, arena?.documento.orEmpty())
                    }
                }
            }.catch { emit(ArenaPayoutsUiState(load = LoadState.Error)) }
        }.onEach { s ->
            _state.update { old -> s?.copy(exportCsv = old.exportCsv) ?: old.copy(load = LoadState.NoArena) }
        }.launchIn(viewModelScope)
    }

    private fun build(repasses: List<Repasse>, projetados: List<Repasse>, p: PayoutPeriod, sel: String?, doc: String): ArenaPayoutsUiState {
        val hoje = clock.now().localDate()
        val all = (repasses.map { it.toUi() } + projetados.map { it.toUi().copy(projected = true) }).sortedByDescending { it.date }
        val from = p.days?.let { hoje.minus(DatePeriod(days = it)) }
        // Futuros (previstos) sempre aparecem; o filtro corta o passado.
        val items = all.filter { from == null || it.date >= from }
        val upcoming = all.filter { it.status == StatusRepasse.AGENDADO && it.date >= hoje }.minByOrNull { it.date }
        return ArenaPayoutsUiState(
            load = LoadState.Ready,
            period = p,
            items = items,
            totalScheduled = Money(items.filter { it.status == StatusRepasse.AGENDADO }.sumOf { it.net.cents }),
            totalPaid = Money(items.filter { it.status == StatusRepasse.LIQUIDADO }.sumOf { it.net.cents }),
            next = upcoming,
            nextAmount = upcoming?.let { u -> Money(all.filter { it.status == StatusRepasse.AGENDADO && it.date == u.date }.sumOf { it.net.cents }) } ?: Money.ZERO,
            heldCount = all.count { it.status == StatusRepasse.RETIDO },
            bankDocument = doc,
            selected = sel?.let { id -> all.firstOrNull { it.id == id } },
        )
    }

    private fun Repasse.toUi() = PayoutItemUi(
        id = id,
        jogoId = jogoId,
        date = dataPrevista,
        dateLabel = ArenaFormat.shortDate(dataPrevista),
        description = descricao.ifBlank { jogoId },
        status = status,
        gross = valorBruto,
        fee = comissao,
        net = valorLiquido,
        expectedLabel = ArenaFormat.fullDate(dataPrevista),
        paidLabel = liquidadoEm?.let { ArenaFormat.fullDate(it.localDate()) },
    )

    fun setPeriod(p: PayoutPeriod) { period.value = p }
    fun select(id: String?) { selectedId.value = id }
    fun retry() {
        _state.update { it.copy(load = LoadState.Loading) }
        retry.update { it + 1 }
    }

    /** Gera o extrato do periodo filtrado (CSV `;` com decimais em virgula, padrao do Excel pt-BR). */
    fun export() {
        val s = _state.value
        val csv = buildCsv(s.items)
        analytics?.track(AnalyticsEvents.EXTRATO_EXPORTADO, mapOf("periodo_dias" to (s.period.days ?: -1), "qtd_repasses" to s.items.size))
        _state.update { it.copy(exportCsv = csv) }
    }

    fun exportHandled() = _state.update { it.copy(exportCsv = null) }

    companion object {
        private fun Money.csv(): String = br.com.sportconnect.core.designsystem.component.formatBrl(cents, withSymbol = false).replace(".", "")

        fun buildCsv(items: List<PayoutItemUi>): String = buildString {
            append("data_prevista;descricao;bruto;taxa;liquido;status;pago_em\n")
            items.forEach { i ->
                append(ArenaFormat.isoDate(i.date)).append(';')
                append(i.description.replace(';', ',')).append(';')
                append(i.gross.csv()).append(';')
                append(i.fee.csv()).append(';')
                append(i.net.csv()).append(';')
                append(
                    when (i.status) {
                        StatusRepasse.AGENDADO -> "programado"
                        StatusRepasse.LIQUIDADO -> "pago"
                        StatusRepasse.RETIDO -> "retido"
                    },
                ).append(';')
                append(i.paidLabel.orEmpty()).append('\n')
            }
        }
    }
}
