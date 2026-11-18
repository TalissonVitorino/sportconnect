package br.com.sportconnect.feature.game

import br.com.sportconnect.core.data.local.GameRating
import br.com.sportconnect.core.data.local.SocialStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.sportconnect.core.domain.repository.AnalyticsEvents
import br.com.sportconnect.core.domain.repository.AnalyticsTracker
import br.com.sportconnect.core.domain.repository.AuthRepository
import br.com.sportconnect.core.domain.WaitlistManager
import br.com.sportconnect.core.domain.usecase.CancelGameUseCase
import br.com.sportconnect.core.domain.usecase.GetGameUseCase
import br.com.sportconnect.core.domain.usecase.LeaveGameUseCase
import br.com.sportconnect.core.domain.usecase.ToggleSpotOfferUseCase
import br.com.sportconnect.core.model.Cota
import br.com.sportconnect.core.model.EtapaPrazo
import br.com.sportconnect.core.model.Jogo
import br.com.sportconnect.core.model.StatusCota
import br.com.sportconnect.core.model.StatusReserva
import br.com.sportconnect.core.model.TipoCota
import br.com.sportconnect.feature.common.ParticipantUi
import br.com.sportconnect.feature.common.SplitCounts
import br.com.sportconnect.feature.common.UiSessionStore
import br.com.sportconnect.feature.common.participantsFor
import br.com.sportconnect.feature.common.splitCounts
import br.com.sportconnect.navigation.Route
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant

/** Estado do detalhe do jogo (10 §2.9; §6.3 Awaiting/Resolving/Confirmed/Expired/Cancelled). */
sealed interface GameDetailUiState {
    data object Loading : GameDetailUiState
    data object NotFound : GameDetailUiState
    data object Error : GameDetailUiState
    data class Content(
        val jogo: Jogo,
        val userId: String?,
        val isOrganizer: Boolean,
        /** Cota paga da pessoa (atleta) ou null. */
        val myPaidShare: Cota?,
        val counts: SplitCounts,
        val participants: List<ParticipantUi>,
        /** Momento de marca "Fechou!" (uma vez por jogo, 10 §3.1). */
        val celebrate: Boolean,
        val now: Instant,
        val busy: Boolean = false,
        /** Lembretes individuais ja enviados (limite 1 a cada 2h). */
        val remindedAt: Map<String, Instant> = emptyMap(),
        val cancelWindowHours: Int = 24,
        /** E14: posicao na lista de espera (1-based) ou null. */
        val waitlistPosition: Int? = null,
        val canJoinWaitlist: Boolean = false,
        /** E15: jogo concluido, participei e ainda nao avaliei. */
        val canRate: Boolean = false,
        /** T8 (13/US-111): posso oferecer minha vaga (pago, CONFIRMADO, ate T-1h, ainda nao oferecida). */
        val canOfferSpot: Boolean = false,
        /** Vaga de outro atleta oferecida a lista de espera, se eu estiver na fila e puder pagar por ela. */
        val offeredSpot: Cota? = null,
    ) : GameDetailUiState {
        val pendingCount: Int get() = jogo.cotasPendentes
        val resolving: Boolean get() = jogo.status == StatusReserva.AGUARDANDO_PAGAMENTOS && jogo.etapaPrazo == EtapaPrazo.JANELA_RESOLUCAO
        val collecting: Boolean get() = jogo.status == StatusReserva.AGUARDANDO_PAGAMENTOS && jogo.etapaPrazo == EtapaPrazo.COLETANDO_COTAS
        val canPay: Boolean get() = !isOrganizer && myPaidShare == null && collecting && jogo.vagasRestantes > 0
        val canLeave: Boolean get() = !isOrganizer && myPaidShare != null && collecting
        /** RN-23: depois da confirmacao o organizador so cancela fora da janela pela arena. */
        val cancelAllowed: Boolean
            get() = when (jogo.status) {
                StatusReserva.AGUARDANDO_PAGAMENTOS -> true
                StatusReserva.CONFIRMADO -> jogo.inicio - now >= cancelWindowHours.hours
                else -> false
            }
    }
}

sealed interface GameDetailEvent {
    /** Abrir o WhatsApp com a mensagem de grupo [pendentes] cotas (I20). */
    data class RemindAll(val jogo: Jogo, val pending: Int) : GameDetailEvent
    data class RemindOne(val jogo: Jogo, val name: String) : GameDetailEvent
    data class RemindLimited(val name: String, val nextAt: Instant) : GameDetailEvent
    /** [reason]: motivo digitado no cancelamento (vai na mensagem do WhatsApp para a galera, E13). */
    data class Cancelled(val reason: String?) : GameDetailEvent
    data object Left : GameDetailEvent
    data class WaitlistJoined(val position: Int) : GameDetailEvent
    data object Rated : GameDetailEvent
    data class Failure(val message: String) : GameDetailEvent
}

class GameDetailViewModel(
    private val route: Route.GameDetail,
    private val getGame: GetGameUseCase,
    private val auth: AuthRepository,
    private val cancelGame: CancelGameUseCase,
    private val leaveGame: LeaveGameUseCase,
    private val analytics: AnalyticsTracker,
    private val session: UiSessionStore,
    private val clock: Clock,
    private val social: SocialStore = SocialStore(),
    private val toggleSpotOffer: ToggleSpotOfferUseCase,
    private val waitlistManager: WaitlistManager = WaitlistManager(),
) : ViewModel() {
    private val _uiState = MutableStateFlow<GameDetailUiState>(GameDetailUiState.Loading)
    val uiState: StateFlow<GameDetailUiState> = _uiState.asStateFlow()
    private val _events = Channel<GameDetailEvent>(Channel.BUFFERED)
    val events: Flow<GameDetailEvent> = _events.receiveAsFlow()

    private var job: Job? = null
    private var celebrating = false
    private var tracked = false
    private val reminded = mutableMapOf<String, Instant>()

    init {
        load()
    }

    fun retry() = load()

    private fun load() {
        job?.cancel()
        job = viewModelScope.launch {
            combine(getGame(route.id), auth.currentUser, social.queues, social.rated) { jogo, user, _, _ -> jogo to user }
                .catch { _uiState.value = GameDetailUiState.Error }
                .collect { (jogo, user) ->
                    if (jogo == null) {
                        _uiState.value = GameDetailUiState.NotFound
                        return@collect
                    }
                    if (!tracked) {
                        tracked = true
                        analytics.track(AnalyticsEvents.JOGO_VISUALIZADO, mapOf("jogo_id" to jogo.id, "origem" to "app"))
                    }
                    val now = clock.now()
                    val uid = user?.id
                    if (jogo.status == StatusReserva.CONFIRMADO && uid != null && jogo.participa(uid) && jogo.id !in session.celebrated.value) {
                        celebrating = true
                        session.markCelebrated(jogo.id)
                    }
                    val prev = _uiState.value as? GameDetailUiState.Content
                    val myPaidShare = uid?.let { id ->
                        jogo.reserva.cotas.firstOrNull { it.atletaId == id && it.status == StatusCota.PAGA && it.tipo == TipoCota.PADRAO }
                    }
                    val waitlistPosition = uid?.let { social.position(jogo.id, it) }
                    _uiState.value = GameDetailUiState.Content(
                        jogo = jogo,
                        userId = uid,
                        isOrganizer = uid != null && jogo.organizadorId == uid,
                        myPaidShare = myPaidShare,
                        counts = jogo.splitCounts(now),
                        participants = jogo.participantsFor(uid, now),
                        celebrate = celebrating && jogo.status == StatusReserva.CONFIRMADO,
                        now = now,
                        busy = prev?.busy ?: false,
                        remindedAt = reminded.toMap(),
                        waitlistPosition = waitlistPosition,
                        canJoinWaitlist = uid != null && jogo.organizadorId != uid && !jogo.participa(uid) &&
                            waitlistPosition == null && social.canJoin(jogo, now),
                        canRate = uid != null && jogo.status == StatusReserva.CONCLUIDO && jogo.participa(uid) && jogo.id !in social.rated.value,
                        // T8 (13/US-111): oferecer a propria vaga, ou pagar a vaga que alguem ofereceu (so quem esta na fila).
                        canOfferSpot = myPaidShare != null && !myPaidShare.offeredForSubstitution && waitlistManager.podeSubstituir(jogo, now),
                        offeredSpot = if (waitlistPosition != null) {
                            jogo.reserva.cotas.firstOrNull { it.offeredForSubstitution && it.status == StatusCota.PAGA && it.atletaId != uid }
                        } else null,
                    )
                }
        }
    }

    /** E14 (US-110): entrar/sair da lista de espera. */
    fun joinWaitlist() {
        val s = _uiState.value as? GameDetailUiState.Content ?: return
        val u = auth.currentUser.value ?: return
        social.join(s.jogo, u.id, u.nome, clock.now()).fold(
            onSuccess = { pos -> _events.trySend(GameDetailEvent.WaitlistJoined(pos)) },
            onFailure = { e -> _events.trySend(GameDetailEvent.Failure(e.message ?: "")) },
        )
    }

    fun leaveWaitlist() {
        val s = _uiState.value as? GameDetailUiState.Content ?: return
        val uid = s.userId ?: return
        social.leave(s.jogo.id, uid)
    }

    /** T8 (13/US-111): oferece ou retira a oferta da minha vaga paga para a lista de espera. */
    fun toggleOfferMySpot(oferecer: Boolean) {
        val s = _uiState.value as? GameDetailUiState.Content ?: return
        val cotaId = s.myPaidShare?.id ?: return
        viewModelScope.launch {
            toggleSpotOffer(s.jogo.id, cotaId, oferecer).fold(
                onSuccess = {},
                onFailure = { e -> _events.trySend(GameDetailEvent.Failure(e.message ?: "")) },
            )
        }
    }

    /** E15 (US-130): avaliacao pos-jogo. */
    fun rate(rating: GameRating) {
        val s = _uiState.value as? GameDetailUiState.Content ?: return
        social.submitRating(s.jogo.id, rating)
        _events.trySend(GameDetailEvent.Rated)
    }

    fun remindAll() {
        val s = _uiState.value as? GameDetailUiState.Content ?: return
        analytics.track(
            AnalyticsEvents.COBRANCA_PENDENTES_ENVIADA,
            mapOf("jogo_id" to s.jogo.id, "qtd_pendentes" to s.pendingCount, "canal" to "whatsapp"),
        )
        _events.trySend(GameDetailEvent.RemindAll(s.jogo, s.pendingCount))
    }

    fun remindOne(p: ParticipantUi) {
        val s = _uiState.value as? GameDetailUiState.Content ?: return
        val now = clock.now()
        val last = reminded[p.key]
        if (last != null && now - last < 2.hours) {
            _events.trySend(GameDetailEvent.RemindLimited(p.name, last + 2.hours))
            return
        }
        reminded[p.key] = now
        _uiState.value = s.copy(remindedAt = reminded.toMap())
        analytics.track(AnalyticsEvents.LEMBRETE_ENVIADO, mapOf("jogo_id" to s.jogo.id, "canal" to "whatsapp", "tipo" to "individual"))
        _events.trySend(GameDetailEvent.RemindOne(s.jogo, p.name))
    }

    fun onInviteShared(canal: String) {
        val s = _uiState.value as? GameDetailUiState.Content ?: return
        analytics.track(AnalyticsEvents.CONVITE_COMPARTILHADO, mapOf("jogo_id" to s.jogo.id, "canal" to canal, "origem" to "detalhe"))
    }

    fun cancel(reason: String? = null) = runBusy {
        cancelGame(route.id, reason).fold(
            onSuccess = { GameDetailEvent.Cancelled(reason?.takeIf { it.isNotBlank() }) },
            onFailure = { GameDetailEvent.Failure(it.message ?: "") },
        )
    }

    fun leave() = runBusy {
        leaveGame(route.id).fold(
            onSuccess = { GameDetailEvent.Left },
            onFailure = { GameDetailEvent.Failure(it.message ?: "") },
        )
    }

    private fun runBusy(block: suspend () -> GameDetailEvent) {
        val s = _uiState.value as? GameDetailUiState.Content ?: return
        if (s.busy) return
        _uiState.value = s.copy(busy = true)
        viewModelScope.launch {
            val ev = block()
            (_uiState.value as? GameDetailUiState.Content)?.let { _uiState.value = it.copy(busy = false) }
            _events.send(ev)
        }
    }
}
