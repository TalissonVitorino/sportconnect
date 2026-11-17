package br.com.sportconnect.feature.invite

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.sportconnect.core.domain.repository.AnalyticsEvents
import br.com.sportconnect.core.domain.repository.AnalyticsTracker
import br.com.sportconnect.core.domain.repository.AuthRepository
import br.com.sportconnect.core.domain.usecase.GetGameUseCase
import br.com.sportconnect.core.domain.usecase.JoinGameByInviteUseCase
import br.com.sportconnect.core.model.Jogo
import br.com.sportconnect.core.model.StatusCota
import br.com.sportconnect.core.model.StatusReserva
import br.com.sportconnect.core.model.TipoCota
import br.com.sportconnect.feature.common.UiSessionStore
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

// ------------------------------------------------------------------ organizador: game/{id}/invite

data class OrganizerInviteUiState(
    val loading: Boolean = true,
    val jogo: Jogo? = null,
    val now: Instant = Instant.DISTANT_PAST,
    /** Mensagem editada pela pessoa (null = usa o texto padrao `wa_invite_message`). */
    val editedMessage: String? = null,
)

class OrganizerInviteViewModel(
    private val route: Route.GameInvite,
    private val getGame: GetGameUseCase,
    private val analytics: AnalyticsTracker,
    private val clock: Clock,
) : ViewModel() {
    private val _uiState = MutableStateFlow(OrganizerInviteUiState())
    val uiState: StateFlow<OrganizerInviteUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            getGame(route.id).collect { j -> _uiState.update { it.copy(loading = false, jogo = j, now = clock.now()) } }
        }
    }

    fun editMessage(text: String) = _uiState.update { it.copy(editedMessage = text) }

    fun onShared(canal: String) {
        analytics.track(AnalyticsEvents.CONVITE_COMPARTILHADO, mapOf("jogo_id" to route.id, "canal" to canal, "origem" to "convite"))
    }
}

// ------------------------------------------------------------------ convidado: invite/{code}

sealed interface GuestInviteUiState {
    data object Loading : GuestInviteUiState
    data class Invalid(val message: String?) : GuestInviteUiState
    data class Content(
        val jogo: Jogo,
        /** 1a abertura neste aparelho (momento de marca §3.2). */
        val firstOpen: Boolean,
        val loggedIn: Boolean,
        /** Organizador abrindo o proprio link -> redireciona para game/{id}. */
        val isOrganizer: Boolean,
        val alreadyPaid: Boolean,
        val declined: Boolean,
        val now: Instant,
    ) : GuestInviteUiState {
        val full: Boolean get() = jogo.status == StatusReserva.CONFIRMADO || (jogo.status == StatusReserva.AGUARDANDO_PAGAMENTOS && jogo.vagasRestantes == 0)
        val cancelled: Boolean get() = jogo.status == StatusReserva.CANCELADO
        val expired: Boolean get() = jogo.status == StatusReserva.EXPIRADO
        val canPay: Boolean get() = !alreadyPaid && !full && !cancelled && !expired && !declined && jogo.status == StatusReserva.AGUARDANDO_PAGAMENTOS
    }
}

class GuestInviteViewModel(
    private val route: Route.Invite,
    private val join: JoinGameByInviteUseCase,
    private val getGame: GetGameUseCase,
    private val auth: AuthRepository,
    private val session: UiSessionStore,
    private val clock: Clock,
) : ViewModel() {
    private val _uiState = MutableStateFlow<GuestInviteUiState>(GuestInviteUiState.Loading)
    val uiState: StateFlow<GuestInviteUiState> = _uiState.asStateFlow()
    private var job: Job? = null
    private var declined = false
    private val firstOpen = route.code.uppercase() !in session.openedInvites.value

    init {
        load()
    }

    fun retry() = load()

    private fun load() {
        job?.cancel()
        job = viewModelScope.launch {
            join(route.code).fold(
                onSuccess = { jogo ->
                    session.markInviteOpened(route.code)
                    combine(getGame(jogo.id), auth.currentUser) { j, u -> j to u }.collect { (j, u) ->
                        if (j == null) {
                            _uiState.value = GuestInviteUiState.Invalid(null)
                            return@collect
                        }
                        _uiState.value = GuestInviteUiState.Content(
                            jogo = j,
                            firstOpen = firstOpen,
                            loggedIn = u != null,
                            isOrganizer = u != null && j.organizadorId == u.id,
                            alreadyPaid = u != null && j.reserva.cotas.any { it.atletaId == u.id && it.status == StatusCota.PAGA && it.tipo == TipoCota.PADRAO },
                            declined = declined,
                            now = clock.now(),
                        )
                    }
                },
                onFailure = { _uiState.value = GuestInviteUiState.Invalid(it.message) },
            )
        }
    }

    fun decline() {
        declined = true
        _uiState.update { (it as? GuestInviteUiState.Content)?.copy(declined = true) ?: it }
    }
}
