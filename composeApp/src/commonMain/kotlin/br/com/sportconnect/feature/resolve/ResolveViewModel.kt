package br.com.sportconnect.feature.resolve

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.sportconnect.core.domain.repository.AnalyticsEvents
import br.com.sportconnect.core.domain.repository.AnalyticsTracker
import br.com.sportconnect.core.domain.repository.AuthRepository
import br.com.sportconnect.core.domain.repository.Cobranca
import br.com.sportconnect.core.domain.repository.PaymentGateway
import br.com.sportconnect.core.domain.usecase.CoverDifferenceUseCase
import br.com.sportconnect.core.domain.usecase.GetGameUseCase
import br.com.sportconnect.core.domain.usecase.PayShareUseCase
import br.com.sportconnect.core.model.Cota
import br.com.sportconnect.core.model.EtapaPrazo
import br.com.sportconnect.core.model.GarantiaCobertura
import br.com.sportconnect.core.model.Jogo
import br.com.sportconnect.core.model.Money
import br.com.sportconnect.core.model.PoliticaNaoPagamento
import br.com.sportconnect.core.model.StatusCota
import br.com.sportconnect.core.model.StatusPagamento
import br.com.sportconnect.core.model.StatusReserva
import br.com.sportconnect.core.model.TipoCota
import br.com.sportconnect.navigation.Route
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.Instant

/** Quem esta decidindo o que (10 §2.11). */
enum class ResolveMode {
    /** A-Pix: organizador paga o que faltou. */
    Cover,
    /** C: pagante decide se aceita o complemento. */
    ResplitPayer,
    /** C: organizador acompanha os aceites. */
    ResplitOrganizer,
    /** Fora da janela (ja resolvido ou nada a fazer). */
    None,
}

data class ResolveUiState(
    val loading: Boolean = true,
    val jogo: Jogo? = null,
    val mode: ResolveMode = ResolveMode.None,
    val missingShares: Int = 0,
    val missingValue: Money = Money.ZERO,
    /** Complemento da pessoa (C pagante). */
    val complement: Cota? = null,
    val accepted: Int = 0,
    val payers: Int = 0,
    val charge: Cobranca? = null,
    val paying: Boolean = false,
    val paid: Boolean = false,
    val declined: Boolean = false,
    val error: String? = null,
    val now: Instant = Instant.DISTANT_PAST,
)

class ResolveViewModel(
    private val route: Route.GameResolve,
    private val getGame: GetGameUseCase,
    private val auth: AuthRepository,
    private val cover: CoverDifferenceUseCase,
    private val payShare: PayShareUseCase,
    private val gateway: PaymentGateway,
    private val analytics: AnalyticsTracker,
    private val clock: Clock,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ResolveUiState())
    val uiState: StateFlow<ResolveUiState> = _uiState.asStateFlow()
    private var watch: Job? = null

    init {
        viewModelScope.launch {
            combine(getGame(route.id), auth.currentUser) { j, u -> j to u }.collect { (j, u) ->
                if (j == null) {
                    _uiState.update { it.copy(loading = false) }
                    return@collect
                }
                val uid = u?.id
                val window = j.status == StatusReserva.AGUARDANDO_PAGAMENTOS && j.etapaPrazo == EtapaPrazo.JANELA_RESOLUCAO
                val complement = j.reserva.cotas.firstOrNull { it.tipo == TipoCota.COMPLEMENTO && it.atletaId == uid && it.status == StatusCota.PENDENTE }
                val complements = j.reserva.cotas.filter { it.tipo == TipoCota.COMPLEMENTO }
                val mode = when {
                    !window -> ResolveMode.None
                    j.politica == PoliticaNaoPagamento.ORGANIZADOR_COBRE && j.garantia == GarantiaCobertura.PIX && j.organizadorId == uid -> ResolveMode.Cover
                    j.politica == PoliticaNaoPagamento.RERATEAR_ENTRE_PAGANTES && complement != null -> ResolveMode.ResplitPayer
                    j.politica == PoliticaNaoPagamento.RERATEAR_ENTRE_PAGANTES && j.organizadorId == uid -> ResolveMode.ResplitOrganizer
                    else -> ResolveMode.None
                }
                val missing = j.reserva.cotas.filter { it.tipo == TipoCota.PADRAO && it.status == StatusCota.PENDENTE }
                _uiState.update {
                    it.copy(
                        loading = false,
                        jogo = j,
                        mode = mode,
                        missingShares = missing.size,
                        missingValue = Money(missing.sumOf { c -> c.valor.cents }),
                        complement = complement ?: it.complement,
                        accepted = complements.count { c -> c.status == StatusCota.PAGA },
                        payers = complements.size,
                        paid = it.paid || j.status == StatusReserva.CONFIRMADO,
                        now = clock.now(),
                    )
                }
            }
        }
    }

    /** A-Pix: gera o Pix do valor faltante (sem taxa). */
    fun payCover() = charge { cover(route.id) }

    /** C: aceitar e pagar o complemento (sem taxa, RN-07). */
    fun accept() {
        analytics.track(AnalyticsEvents.RERRATEIO_RESPONDIDO, mapOf("jogo_id" to route.id, "resposta" to "aceitou"))
        charge { payShare(route.id) }
    }

    /** C: recusar -> o complemento expira em H e a cota original e reembolsada (RN-16). */
    fun decline() {
        analytics.track(AnalyticsEvents.RERRATEIO_RESPONDIDO, mapOf("jogo_id" to route.id, "resposta" to "recusou"))
        _uiState.update { it.copy(declined = true) }
    }

    private fun charge(block: suspend () -> Result<Cobranca>) {
        if (_uiState.value.paying) return
        _uiState.update { it.copy(paying = true, error = null) }
        viewModelScope.launch {
            block().fold(
                onSuccess = { cob ->
                    _uiState.update { it.copy(charge = cob, paying = false) }
                    watch?.cancel()
                    watch = launch {
                        gateway.observePagamento(cob.pagamentoId).collect { p ->
                            if (p?.status == StatusPagamento.CONFIRMADO) _uiState.update { it.copy(paid = true) }
                        }
                    }
                },
                onFailure = { e -> _uiState.update { it.copy(paying = false, error = e.message) } },
            )
        }
    }
}
