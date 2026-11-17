package br.com.sportconnect.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.sportconnect.core.domain.repository.AnalyticsEvents
import br.com.sportconnect.core.domain.repository.AnalyticsTracker
import br.com.sportconnect.core.domain.repository.AuthRepository
import br.com.sportconnect.core.domain.usecase.GetMyGamesUseCase
import br.com.sportconnect.core.domain.usecase.GetNotificationsUseCase
import br.com.sportconnect.core.model.EtapaPrazo
import br.com.sportconnect.core.model.Jogo
import br.com.sportconnect.core.model.PoliticaNaoPagamento
import br.com.sportconnect.core.model.GarantiaCobertura
import br.com.sportconnect.core.model.StatusCota
import br.com.sportconnect.core.model.StatusReserva
import br.com.sportconnect.core.model.TipoCota
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.Instant

/** Papel do usuario no card. */
enum class HomeRole { Organizer, Participant }

data class HomeGame(
    val jogo: Jogo,
    val role: HomeRole,
)

/** Estados da Home (10 §6.3: Loading/Empty/Content/Error). */
sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Empty(val firstName: String, val unread: Int) : HomeUiState
    data class Content(
        val firstName: String,
        val userId: String,
        /** Decisao de regra pendente (resolve). */
        val decisions: List<HomeGame>,
        /** Cota a pagar (organizador em rascunho, complemento, checkout iniciado). */
        val pending: List<HomeGame>,
        /** Proximos: organizo em pre-reserva (prazo mais proximo) -> confirmados (T mais proximo). */
        val upcoming: List<HomeGame>,
        val unread: Int,
        val now: Instant,
    ) : HomeUiState
    data object Error : HomeUiState
}

class HomeViewModel(
    private val getMyGames: GetMyGamesUseCase,
    private val getNotifications: GetNotificationsUseCase,
    private val auth: AuthRepository,
    private val analytics: AnalyticsTracker,
    private val clock: Clock,
) : ViewModel() {
    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    private var job: Job? = null

    init {
        load()
    }

    fun retry() = load()

    fun onCreateGame(origem: String = "home") {
        analytics.track(AnalyticsEvents.JOGO_CRIACAO_INICIADA, mapOf("origem" to origem))
    }

    private fun load() {
        job?.cancel()
        _uiState.value = HomeUiState.Loading
        job = viewModelScope.launch {
            combine(getMyGames(), getNotifications(), auth.currentUser) { meus, avisos, user ->
                val firstName = user?.primeiroNome.orEmpty()
                val userId = user?.id
                val unread = avisos.count { !it.lido }
                if (userId == null) return@combine HomeUiState.Empty(firstName, unread)
                val now = clock.now()
                val ativos = meus.proximos
                fun role(j: Jogo) = if (j.organizadorId == userId) HomeRole.Organizer else HomeRole.Participant
                val decisions = ativos.filter { needsDecision(it, userId) }
                val pending = ativos.filter { it !in decisions && needsPayment(it, userId) }
                val upcoming = ativos.filter { it !in decisions && it !in pending && it.status != StatusReserva.RASCUNHO }
                    .sortedWith(
                        compareBy<Jogo>(
                            { if (it.status == StatusReserva.AGUARDANDO_PAGAMENTOS) 0 else 1 },
                            { if (it.status == StatusReserva.AGUARDANDO_PAGAMENTOS) it.reserva.prazoCotas ?: it.inicio else it.inicio },
                        ),
                    )
                if (decisions.isEmpty() && pending.isEmpty() && upcoming.isEmpty()) {
                    HomeUiState.Empty(firstName, unread)
                } else {
                    HomeUiState.Content(
                        firstName = firstName,
                        userId = userId,
                        decisions = decisions.map { HomeGame(it, role(it)) },
                        pending = pending.map { HomeGame(it, role(it)) },
                        upcoming = upcoming.map { HomeGame(it, role(it)) },
                        unread = unread,
                        now = now,
                    )
                }
            }.catch { _uiState.value = HomeUiState.Error }
                .collect { _uiState.value = it }
        }
    }

    companion object {
        /** Janela de resolucao em que a pessoa precisa responder (A-Pix organizador, C pagante). */
        fun needsDecision(j: Jogo, userId: String): Boolean {
            if (j.status != StatusReserva.AGUARDANDO_PAGAMENTOS || j.etapaPrazo != EtapaPrazo.JANELA_RESOLUCAO) return false
            return when (j.politica) {
                PoliticaNaoPagamento.ORGANIZADOR_COBRE -> j.organizadorId == userId && j.garantia == GarantiaCobertura.PIX
                PoliticaNaoPagamento.RERATEAR_ENTRE_PAGANTES -> j.reserva.cotas.any {
                    it.tipo == TipoCota.COMPLEMENTO && it.atletaId == userId && it.status == StatusCota.PENDENTE
                }
                PoliticaNaoPagamento.CANCELAR_E_REEMBOLSAR -> false
            }
        }

        /** Cota pendente da propria pessoa (organizador em rascunho ou checkout iniciado e nao pago). */
        fun needsPayment(j: Jogo, userId: String): Boolean = when {
            j.status == StatusReserva.RASCUNHO -> j.organizadorId == userId
            j.status == StatusReserva.AGUARDANDO_PAGAMENTOS && j.etapaPrazo == EtapaPrazo.COLETANDO_COTAS ->
                j.reserva.cotas.none { it.atletaId == userId && it.status == StatusCota.PAGA && it.tipo == TipoCota.PADRAO } &&
                    j.reserva.cotas.any { it.atletaId == userId && it.status == StatusCota.PENDENTE }
            else -> false
        }
    }
}
