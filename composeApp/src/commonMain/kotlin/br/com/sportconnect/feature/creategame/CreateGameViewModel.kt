package br.com.sportconnect.feature.creategame

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.sportconnect.core.domain.HoldDeadlineCalculator
import br.com.sportconnect.core.domain.Rateio
import br.com.sportconnect.core.domain.SplitCostUseCase
import br.com.sportconnect.core.domain.repository.AnalyticsEvents
import br.com.sportconnect.core.domain.repository.AnalyticsTracker
import br.com.sportconnect.core.domain.repository.ArenaRepository
import br.com.sportconnect.core.domain.usecase.CreateGameUseCase
import br.com.sportconnect.core.domain.usecase.NovoJogo
import br.com.sportconnect.core.domain.usecase.RegraNegocioException
import br.com.sportconnect.core.model.Arena
import br.com.sportconnect.core.model.GarantiaCobertura
import br.com.sportconnect.core.model.Money
import br.com.sportconnect.core.model.PoliticaNaoPagamento
import br.com.sportconnect.core.model.Quadra
import br.com.sportconnect.core.model.Slot
import br.com.sportconnect.core.model.Sport
import br.com.sportconnect.navigation.Route
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

/** Passos do stepper (10 §2.7). */
enum class CreateStep { Court, Split, Rule, Review }

/** Opcao de regra exibida no passo 3 (A-cartao e A-Pix contam como uma regra com garantia). */
enum class RuleOption { Covers, Cancel, Resplit }

enum class SpotsError { Range, MinShare }

/** Erros de criacao mapeados para as strings do 07. */
enum class CreateError { SlotTaken, DeadlineTooShort, CheckoutExpired, PriceChanged, SessionExpired, Generic }

data class CreateGameUiState(
    val loading: Boolean = true,
    val step: CreateStep = CreateStep.Court,
    val slot: Slot? = null,
    val quadra: Quadra? = null,
    val arena: Arena? = null,
    val sports: List<Sport> = emptyList(),
    val sport: Sport? = null,
    val name: String = "",
    val spots: Int = 0,
    val minSpots: Int = 2,
    val maxSpots: Int = 20,
    val rateio: Rateio? = null,
    val spotsError: SpotsError? = null,
    val rule: RuleOption = RuleOption.Covers,
    val guarantee: GarantiaCobertura = GarantiaCobertura.CARTAO,
    /** RN-16: teto do complemento em % da cota (padrao 50). */
    val capPct: Int = 50,
    /** Viabilidade por regra (RN-12). A: viavel se ao menos uma garantia couber. */
    val viable: Map<RuleOption, Boolean> = emptyMap(),
    val guaranteeViable: Map<GarantiaCobertura, Boolean> = emptyMap(),
    /** Prazo das cotas (P) para a regra/garantia atual, calculado como se ativasse agora. */
    val deadline: Instant? = null,
    val hold: Instant? = null,
    val submitting: Boolean = false,
    val error: CreateError? = null,
    val errorMessage: String? = null,
    val now: Instant = Instant.DISTANT_PAST,
) {
    val politica: PoliticaNaoPagamento
        get() = when (rule) {
            RuleOption.Covers -> PoliticaNaoPagamento.ORGANIZADOR_COBRE
            RuleOption.Cancel -> PoliticaNaoPagamento.CANCELAR_E_REEMBOLSAR
            RuleOption.Resplit -> PoliticaNaoPagamento.RERATEAR_ENTRE_PAGANTES
        }
    val garantiaEfetiva: GarantiaCobertura get() = if (rule == RuleOption.Covers) guarantee else GarantiaCobertura.NAO_SE_APLICA

    /** Valor que o organizador cobre se ninguem mais pagar (A): V − cota do organizador. */
    val exposure: Money get() = rateio?.let { it.valorTotal - it.cotaOrganizador } ?: Money.ZERO

    /** Exemplo de 2 faltantes (US-022). */
    val twoMissing: Money get() = rateio?.let { it.cotaConvidado * 2 } ?: Money.ZERO
    val resplitPayers: Int get() = (spots - 2).coerceAtLeast(1)
    val resplitExtra: Money get() = rateio?.let { Money((twoMissing.cents + resplitPayers - 1) / resplitPayers) } ?: Money.ZERO
    val capValue: Money get() = rateio?.let { Money(it.cotaConvidado.cents * capPct / 100) } ?: Money.ZERO

    /** O organizador paga agora: a propria cota (com a sobra, RN-04) + taxa Pix. */
    val payNow: Money get() = rateio?.let { it.cotaOrganizador + it.taxaConveniencia } ?: Money.ZERO

    val hasData: Boolean get() = name.isNotBlank() || step != CreateStep.Court
    val canContinue: Boolean
        get() = when (step) {
            CreateStep.Court -> slot != null && sport != null
            CreateStep.Split -> rateio != null && spotsError == null
            CreateStep.Rule -> viable[rule] == true && (rule != RuleOption.Covers || guaranteeViable[guarantee] == true)
            CreateStep.Review -> !submitting
        }
}

sealed interface CreateGameEvent {
    data class GoToPay(val gameId: String) : CreateGameEvent
}

class CreateGameViewModel(
    private val route: Route.GameCreate,
    private val arenas: ArenaRepository,
    private val split: SplitCostUseCase,
    private val holdCalculator: HoldDeadlineCalculator,
    private val createGame: CreateGameUseCase,
    private val analytics: AnalyticsTracker,
    private val clock: Clock,
) : ViewModel() {
    private val _uiState = MutableStateFlow(CreateGameUiState(now = clock.now()))
    val uiState: StateFlow<CreateGameUiState> = _uiState.asStateFlow()
    private val _events = Channel<CreateGameEvent>(Channel.BUFFERED)
    val events: Flow<CreateGameEvent> = _events.receiveAsFlow()

    init {
        analytics.track(AnalyticsEvents.JOGO_CRIACAO_INICIADA, mapOf("origem" to if (route.slotId != null) "horario" else "home"))
        viewModelScope.launch { load() }
    }

    private suspend fun load() {
        val slotId = route.slotId
        if (slotId == null) {
            _uiState.update { it.copy(loading = false) }
            return
        }
        val slot = arenas.slot(slotId)
        val quadra = slot?.let { arenas.quadra(it.quadraId) }
        val arena = slot?.let { arenas.arena(it.arenaId) }
        val sports = quadra?.esportes?.map { it.esporte }.orEmpty()
        val sport = sports.firstOrNull()
        _uiState.update {
            it.copy(loading = false, slot = slot, quadra = quadra, arena = arena, sports = sports)
        }
        if (sport != null) selectSport(sport)
    }

    fun selectSport(sport: Sport) {
        val q = _uiState.value.quadra ?: return
        val cap = q.capacidadePara(sport) ?: sport.maxVagas
        val max = minOf(sport.maxVagas, cap)
        val spots = sport.vagasPadrao.coerceIn(sport.minVagas, max)
        _uiState.update { it.copy(sport = sport, minSpots = sport.minVagas, maxSpots = max) }
        setSpots(spots)
    }

    fun setName(name: String) = _uiState.update { it.copy(name = name.take(40)) }

    fun incrementSpots() = setSpots(_uiState.value.spots + 1)
    fun decrementSpots() = setSpots(_uiState.value.spots - 1)

    fun setSpots(n: Int) {
        val s = _uiState.value
        val slot = s.slot ?: return
        val value = n.coerceIn(1, 20)
        val rateio = split.calcular(slot.preco, value)
        val err = when {
            value < s.minSpots || value > s.maxSpots -> SpotsError.Range
            !rateio.respeitaCotaMinima -> SpotsError.MinShare
            else -> null
        }
        _uiState.update { it.copy(spots = value, rateio = rateio, spotsError = err) }
        recompute()
    }

    fun selectRule(r: RuleOption) {
        if (_uiState.value.viable[r] == false) return
        _uiState.update { it.copy(rule = r) }
        recompute()
    }

    fun selectGuarantee(g: GarantiaCobertura) {
        if (_uiState.value.guaranteeViable[g] == false) return
        _uiState.update { it.copy(guarantee = g) }
        recompute()
    }

    fun setCapPct(pct: Int) = _uiState.update { it.copy(capPct = pct.coerceIn(0, 100)) }

    /** Viabilidade (RN-12) e prazos (RN-10/11), como se a pre-reserva fosse ativada agora. */
    private fun recompute() {
        val s = _uiState.value
        val slot = s.slot ?: return
        val holdHoras = s.arena?.duracaoHoldHoras ?: HoldDeadlineCalculator.HOLD_PADRAO_HORAS
        val now = clock.now()
        fun ok(p: PoliticaNaoPagamento, g: GarantiaCobertura) = holdCalculator.politicaViavel(now, slot.inicio, p, g, holdHoras)
        val gv = mapOf(
            GarantiaCobertura.CARTAO to ok(PoliticaNaoPagamento.ORGANIZADOR_COBRE, GarantiaCobertura.CARTAO),
            GarantiaCobertura.PIX to ok(PoliticaNaoPagamento.ORGANIZADOR_COBRE, GarantiaCobertura.PIX),
        )
        val viable = mapOf(
            RuleOption.Covers to (gv.values.any { it }),
            RuleOption.Cancel to ok(PoliticaNaoPagamento.CANCELAR_E_REEMBOLSAR, GarantiaCobertura.NAO_SE_APLICA),
            RuleOption.Resplit to ok(PoliticaNaoPagamento.RERATEAR_ENTRE_PAGANTES, GarantiaCobertura.NAO_SE_APLICA),
        )
        var rule = s.rule
        if (viable[rule] != true) rule = RuleOption.entries.firstOrNull { viable[it] == true } ?: rule
        var guarantee = s.guarantee
        if (gv[guarantee] != true) guarantee = gv.entries.firstOrNull { it.value }?.key ?: guarantee
        val pol = when (rule) {
            RuleOption.Covers -> PoliticaNaoPagamento.ORGANIZADOR_COBRE
            RuleOption.Cancel -> PoliticaNaoPagamento.CANCELAR_E_REEMBOLSAR
            RuleOption.Resplit -> PoliticaNaoPagamento.RERATEAR_ENTRE_PAGANTES
        }
        val gar = if (rule == RuleOption.Covers) guarantee else GarantiaCobertura.NAO_SE_APLICA
        val prazos = runCatching { holdCalculator.calcular(now, slot.inicio, pol, gar, holdHoras) }.getOrNull()
        _uiState.update {
            it.copy(
                viable = viable, guaranteeViable = gv, rule = rule, guarantee = guarantee,
                deadline = prazos?.cotas, hold = prazos?.hold, now = now,
            )
        }
    }

    fun next() {
        val s = _uiState.value
        if (!s.canContinue) return
        when (s.step) {
            CreateStep.Court -> _uiState.update { it.copy(step = CreateStep.Split) }
            CreateStep.Split -> _uiState.update { it.copy(step = CreateStep.Rule) }
            CreateStep.Rule -> {
                analytics.track(
                    AnalyticsEvents.JOGO_CONFIGURADO,
                    mapOf("politica" to s.politica.name, "garantia" to s.garantiaEfetiva.name.lowercase(), "vagas" to s.spots, "etapa" to "revisao"),
                )
                _uiState.update { it.copy(step = CreateStep.Review) }
            }
            CreateStep.Review -> submit()
        }
    }

    /** Volta um passo; retorna false no passo 1 (a tela decide se pede confirmacao para sair). */
    fun back(): Boolean {
        val s = _uiState.value
        val prev = when (s.step) {
            CreateStep.Court -> return false
            CreateStep.Split -> CreateStep.Court
            CreateStep.Rule -> CreateStep.Split
            CreateStep.Review -> CreateStep.Rule
        }
        _uiState.update { it.copy(step = prev, error = null) }
        return true
    }

    fun goTo(step: CreateStep) {
        if (step.ordinal < _uiState.value.step.ordinal) _uiState.update { it.copy(step = step, error = null) }
    }

    fun dismissError() = _uiState.update { it.copy(error = null, errorMessage = null) }

    private fun submit() {
        val s = _uiState.value
        val slot = s.slot ?: return
        val sport = s.sport ?: return
        _uiState.update { it.copy(submitting = true, error = null) }
        viewModelScope.launch {
            // E4: a arena pode ter mudado o preco entre a escolha e a confirmacao. Mostra o novo valor e o
            // novo rateio antes de seguir (`error_price_changed_*`); confirmar de novo cria com o preco atual.
            val current = runCatching { arenas.slot(slot.id) }.getOrNull()
            if (current != null && current.preco != slot.preco) {
                _uiState.update { it.copy(submitting = false, slot = current, error = CreateError.PriceChanged, errorMessage = current.preco.format()) }
                analytics.track("erro_exibido", mapOf("error_id" to CreateError.PriceChanged.name))
                return@launch
            }
            createGame(
                NovoJogo(
                    slotId = slot.id,
                    esporte = sport,
                    numeroVagas = s.spots,
                    politica = s.politica,
                    garantia = s.garantiaEfetiva,
                    tetoComplementoPct = s.capPct,
                    titulo = s.name.ifBlank { null },
                ),
            ).fold(
                onSuccess = { jogo ->
                    _uiState.update { it.copy(submitting = false) }
                    _events.send(CreateGameEvent.GoToPay(jogo.id))
                },
                onFailure = { e ->
                    val err = when ((e as? RegraNegocioException)?.codigo) {
                        "SLOT_INDISPONIVEL" -> CreateError.SlotTaken
                        "POLITICA_INVIAVEL" -> CreateError.DeadlineTooShort
                        "NAO_AUTENTICADO" -> CreateError.SessionExpired
                        else -> CreateError.Generic
                    }
                    analytics.track("erro_exibido", mapOf("error_id" to err.name))
                    _uiState.update { it.copy(submitting = false, error = err, errorMessage = e.message) }
                },
            )
        }
    }

    companion object {
        /** Antecedencia minima mostrada no cartao inviavel (I4): A-Pix/C = 3h; B/A-cartao = 2h30. */
        fun minLead(rule: RuleOption, g: GarantiaCobertura): Duration = when {
            rule == RuleOption.Resplit -> 3.hours
            rule == RuleOption.Covers && g == GarantiaCobertura.PIX -> 3.hours
            else -> 2.hours + 30.minutes
        }
    }
}
