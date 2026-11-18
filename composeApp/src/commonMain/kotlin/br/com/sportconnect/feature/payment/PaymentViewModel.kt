package br.com.sportconnect.feature.payment

import br.com.sportconnect.core.model.FusoSaoPaulo
import kotlinx.datetime.toLocalDateTime
import br.com.sportconnect.core.data.local.AppPreferences
import br.com.sportconnect.core.domain.CardValidation
import br.com.sportconnect.core.domain.CardBrand
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.sportconnect.core.domain.repository.AnalyticsEvents
import br.com.sportconnect.core.domain.repository.AnalyticsTracker
import br.com.sportconnect.core.domain.repository.AuthRepository
import br.com.sportconnect.core.domain.repository.Cobranca
import br.com.sportconnect.core.domain.repository.PaymentGateway
import br.com.sportconnect.core.domain.usecase.ClaimOfferedSpotUseCase
import br.com.sportconnect.core.domain.usecase.GetGameUseCase
import br.com.sportconnect.core.domain.usecase.PayShareUseCase
import br.com.sportconnect.core.domain.usecase.RegraNegocioException
import br.com.sportconnect.core.model.EtapaPrazo
import br.com.sportconnect.core.model.Jogo
import br.com.sportconnect.core.model.MetodoPagamento
import br.com.sportconnect.core.model.Money
import br.com.sportconnect.core.model.StatusCota
import br.com.sportconnect.core.model.StatusPagamento
import br.com.sportconnect.core.model.StatusReserva
import br.com.sportconnect.core.model.TaxaConveniencia
import br.com.sportconnect.core.model.TipoCota
import br.com.sportconnect.navigation.Route
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

/** Fases do pagamento (10 §6.3: Idle/Generating/Waiting/Processing/Paid/Expired/Error). */
sealed interface PixPhase {
    data object Idle : PixPhase
    data object Generating : PixPhase
    data class Waiting(val cobranca: Cobranca) : PixPhase
    data class Processing(val cobranca: Cobranca) : PixPhase
    data class Paid(val cobranca: Cobranca) : PixPhase
    data class Expired(val cobranca: Cobranca) : PixPhase
    /** [code] = codigo de [RegraNegocioException] (JA_PAGOU, SEM_VAGAS...). */
    data class Error(val code: String?, val message: String) : PixPhase
}

data class CardForm(
    val number: String = "",
    val expiry: String = "",
    val cvv: String = "",
    val name: String = "",
    val save: Boolean = false,
    /** true depois da 1a tentativa de pagar: so entao os erros aparecem. */
    val showErrors: Boolean = false,
) {
    val brand: CardBrand get() = CardValidation.brand(number)
    val numberOk: Boolean get() = CardValidation.numberValid(number)
    val cvvOk: Boolean get() = CardValidation.cvvValid(cvv, brand)
    val nameOk: Boolean get() = name.trim().length >= 3
}

data class PaymentUiState(
    val loading: Boolean = true,
    val jogo: Jogo? = null,
    val shareValue: Money = Money.ZERO,
    val method: MetodoPagamento = MetodoPagamento.PIX,
    /** Complemento/cobertura nao tem taxa (RN-07, I22). */
    val noFee: Boolean = false,
    val phase: PixPhase = PixPhase.Idle,
    /** Trava de 10min da vaga (RN-19). */
    val lockUntil: Instant? = null,
    val copied: Boolean = false,
    val afterCreate: Boolean = false,
    val loggedIn: Boolean = true,
    /** Quando o Pix entrou em "aguardando" (E4: depois de 2 min mostra `error_payment_slow_*`). */
    val waitingSince: Instant? = null,
    /** E5: formulario do cartao (fica so na memoria da tela; o gateway tokeniza em producao). */
    val card: CardForm = CardForm(),
) {
    val fee: Money get() = if (noFee) Money.ZERO else TaxaConveniencia.para(method)
    val total: Money get() = shareValue + fee
    val paid: Boolean get() = phase is PixPhase.Paid
}

class PaymentViewModel(
    private val route: Route.GamePay,
    private val getGame: GetGameUseCase,
    private val payShare: PayShareUseCase,
    private val claimOfferedSpot: ClaimOfferedSpotUseCase,
    private val gateway: PaymentGateway,
    private val auth: AuthRepository,
    private val analytics: AnalyticsTracker,
    private val clock: Clock,
    private val prefs: AppPreferences? = null,
) : ViewModel() {
    private val _uiState = MutableStateFlow(PaymentUiState(afterCreate = route.afterCreate, lockUntil = clock.now() + 10.minutes))
    val uiState: StateFlow<PaymentUiState> = _uiState.asStateFlow()
    private var watch: Job? = null

    init {
        viewModelScope.launch {
            combine(getGame(route.id), auth.currentUser) { j, u -> j to u }.collect { (jogo, user) ->
                _uiState.update { s ->
                    if (jogo == null) return@update s.copy(loading = false, phase = PixPhase.Error("JOGO_INEXISTENTE", ""))
                    val uid = user?.id
                    val complemento = uid?.let { id ->
                        jogo.reserva.cotas.firstOrNull { it.tipo == TipoCota.COMPLEMENTO && it.atletaId == id && it.status == StatusCota.PENDENTE }
                    }
                    val share = when {
                        // T8 (13/US-111): mesmo valor da cota oferecida, com taxa normal (quem saiu que nao paga taxa).
                        route.substituicaoCotaId != null ->
                            jogo.reserva.cotas.firstOrNull { it.id == route.substituicaoCotaId }?.valor ?: jogo.valorCotaConvidado
                        jogo.status == StatusReserva.RASCUNHO -> jogo.cotaOrganizador?.valor ?: jogo.valorCotaConvidado
                        jogo.etapaPrazo == EtapaPrazo.JANELA_RESOLUCAO && complemento != null -> complemento.valor
                        else -> jogo.valorCotaConvidado
                    }
                    s.copy(
                        loading = false,
                        jogo = jogo,
                        shareValue = if (s.phase is PixPhase.Idle || s.phase is PixPhase.Error) share else s.shareValue,
                        noFee = complemento != null && jogo.etapaPrazo == EtapaPrazo.JANELA_RESOLUCAO,
                        loggedIn = user != null,
                    )
                }
            }
        }
    }

    fun updateCard(f: (CardForm) -> CardForm) = _uiState.update { it.copy(card = f(it.card)) }

    private fun today() = clock.now().toLocalDateTime(FusoSaoPaulo).date

    fun selectMethod(m: MetodoPagamento) {
        if (_uiState.value.phase !is PixPhase.Idle && _uiState.value.phase !is PixPhase.Error) return
        _uiState.update { it.copy(method = m) }
        analytics.track(AnalyticsEvents.METODO_PAGAMENTO_SELECIONADO, mapOf("jogo_id" to route.id, "metodo" to m.name.lowercase()))
    }

    /** Gera a cobranca (Pix copia-e-cola ou cartao) e passa a observar o status no gateway. */
    fun pay() {
        val s = _uiState.value
        if (s.phase is PixPhase.Generating || s.phase is PixPhase.Waiting || s.phase is PixPhase.Paid) return
        if (s.method == MetodoPagamento.CARTAO) {
            val c = s.card
            val expiry = CardValidation.expiryState(c.expiry, today())
            if (expiry == CardValidation.ExpiryState.Expired) {
                // E4: cartao vencido (`error_card_expired_*`).
                _uiState.update { it.copy(card = c.copy(showErrors = true), phase = PixPhase.Error("CARTAO_VENCIDO", "")) }
                return
            }
            if (!c.numberOk || expiry != CardValidation.ExpiryState.Ok || !c.cvvOk || !c.nameOk) {
                _uiState.update { it.copy(card = c.copy(showErrors = true)) }
                return
            }
            if (c.number.filter(Char::isDigit) == CardValidation.DECLINED_TEST_NUMBER) {
                // Fake: o banco recusa (`error_payment_declined_*`).
                analytics.track(AnalyticsEvents.ERRO_EXIBIDO_FALLBACK, mapOf("error_id" to "cartao_recusado"))
                _uiState.update { it.copy(phase = PixPhase.Error("CARTAO_RECUSADO", "")) }
                return
            }
            if (c.save) {
                val digits = c.number.filter(Char::isDigit)
                val saved = AppPreferences.SavedCard(c.brand.label, digits.takeLast(4), c.expiry.take(2) + "/" + c.expiry.drop(2))
                prefs?.let { p -> p.savedCards = (p.savedCards.filterNot { it.last4 == saved.last4 && it.brand == saved.brand } + saved) }
            }
        }
        _uiState.update { it.copy(phase = PixPhase.Generating, copied = false) }
        viewModelScope.launch {
            val resultado = if (route.substituicaoCotaId != null) {
                claimOfferedSpot(route.id, route.substituicaoCotaId, s.method)
            } else {
                payShare(route.id, s.method)
            }
            resultado.fold(
                onSuccess = { cob ->
                    _uiState.update {
                        it.copy(
                            phase = if (s.method == MetodoPagamento.CARTAO) PixPhase.Processing(cob) else PixPhase.Waiting(cob),
                            waitingSince = clock.now(),
                            shareValue = cob.valorCota,
                            noFee = cob.taxaConveniencia.isZero,
                            lockUntil = cob.expiraEm,
                        )
                    }
                    observe(cob)
                },
                onFailure = { e ->
                    val code = (e as? RegraNegocioException)?.codigo
                    analytics.track(AnalyticsEvents.ERRO_EXIBIDO_FALLBACK, mapOf("error_id" to (code ?: "generico")))
                    _uiState.update { it.copy(phase = PixPhase.Error(code, e.message ?: "")) }
                },
            )
        }
    }

    private fun observe(cob: Cobranca) {
        watch?.cancel()
        watch = viewModelScope.launch {
            gateway.observePagamento(cob.pagamentoId).collect { p ->
                when (p?.status) {
                    StatusPagamento.CONFIRMADO -> _uiState.update { it.copy(phase = PixPhase.Paid(cob)) }
                    StatusPagamento.EXPIRADO -> _uiState.update { it.copy(phase = PixPhase.Expired(cob)) }
                    StatusPagamento.FALHOU -> _uiState.update { it.copy(phase = PixPhase.Error("PAGAMENTO_FALHOU", "")) }
                    else -> Unit
                }
            }
        }
    }

    fun onCopied() {
        val cob = (_uiState.value.phase as? PixPhase.Waiting)?.cobranca
        analytics.track(AnalyticsEvents.PIX_CODIGO_COPIADO, mapOf("jogo_id" to route.id, "pagamento_id" to cob?.pagamentoId))
        _uiState.update { it.copy(copied = true) }
        viewModelScope.launch {
            delay(2_000)
            _uiState.update { it.copy(copied = false) }
        }
    }

    /** "Já paguei": mostra "Confirmando com o banco…" e consulta o gateway (o fake confirma na hora). */
    fun checkNow() {
        val cob = (_uiState.value.phase as? PixPhase.Waiting)?.cobranca ?: return
        _uiState.update { it.copy(phase = PixPhase.Processing(cob)) }
        viewModelScope.launch {
            delay(300)
            gateway.simularConfirmacao(cob.pagamentoId)
        }
    }

    fun regenerate() {
        _uiState.update { it.copy(phase = PixPhase.Idle) }
        pay()
    }

    fun payWithCard() {
        _uiState.update { it.copy(phase = PixPhase.Idle, method = MetodoPagamento.CARTAO) }
    }

    /** Cartao recusado -> "Pagar com Pix" (`error_payment_declined_action`). */
    fun switchToPix() {
        _uiState.update { it.copy(phase = PixPhase.Idle, method = MetodoPagamento.PIX) }
    }

    /** Volta ao formulario do cartao (ex.: depois de "cartao vencido"). */
    fun backToForm() {
        _uiState.update { it.copy(phase = PixPhase.Idle) }
    }
}

/** Evento `erro_exibido` (04 §6.2) — nome local enquanto o catalogo de eventos nao o expoe. */
private val AnalyticsEvents.ERRO_EXIBIDO_FALLBACK: String get() = "erro_exibido"
