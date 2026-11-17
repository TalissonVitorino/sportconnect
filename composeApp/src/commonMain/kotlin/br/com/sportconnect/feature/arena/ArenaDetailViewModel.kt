package br.com.sportconnect.feature.arena

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.sportconnect.core.domain.repository.AnalyticsEvents
import br.com.sportconnect.core.domain.repository.AnalyticsTracker
import br.com.sportconnect.core.domain.usecase.ArenaDetalhe
import br.com.sportconnect.core.domain.usecase.GetArenaDetailUseCase
import br.com.sportconnect.core.domain.usecase.GetAvailableSlotsUseCase
import br.com.sportconnect.core.model.FusoSaoPaulo
import br.com.sportconnect.core.model.Slot
import br.com.sportconnect.core.model.Sport
import br.com.sportconnect.core.model.StatusArena
import br.com.sportconnect.navigation.Route
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

sealed interface ArenaDetailUiState {
    data object Loading : ArenaDetailUiState
    data object NotFound : ArenaDetailUiState
    /** Arena despublicada/suspensa (10 §2.5). */
    data object Unpublished : ArenaDetailUiState
    data class Content(
        val detail: ArenaDetalhe,
        val sport: Sport?,
        /** Horarios livres de hoje por quadra (mini-grade). */
        val todaySlots: Map<String, List<Slot>>,
    ) : ArenaDetailUiState
}

class ArenaDetailViewModel(
    private val route: Route.ArenaDetail,
    private val getDetail: GetArenaDetailUseCase,
    private val getSlots: GetAvailableSlotsUseCase,
    private val analytics: AnalyticsTracker,
    private val clock: Clock,
) : ViewModel() {
    private val _uiState = MutableStateFlow<ArenaDetailUiState>(ArenaDetailUiState.Loading)
    val uiState: StateFlow<ArenaDetailUiState> = _uiState.asStateFlow()
    private var slotsJob: Job? = null

    init {
        viewModelScope.launch {
            val d = getDetail(route.id)
            when {
                d == null -> _uiState.value = ArenaDetailUiState.NotFound
                d.arena.status != StatusArena.ATIVA -> _uiState.value = ArenaDetailUiState.Unpublished
                else -> {
                    analytics.track(AnalyticsEvents.ARENA_VISUALIZADA, mapOf("arena_id" to d.arena.id, "origem" to "busca"))
                    _uiState.value = ArenaDetailUiState.Content(d, null, emptyMap())
                    observeSlots(null)
                }
            }
        }
    }

    fun selectSport(s: Sport?) {
        val c = _uiState.value as? ArenaDetailUiState.Content ?: return
        val next = if (c.sport == s) null else s
        _uiState.value = c.copy(sport = next)
        observeSlots(next)
    }

    private fun observeSlots(sport: Sport?) {
        slotsJob?.cancel()
        val today = clock.now().toLocalDateTime(FusoSaoPaulo).date
        slotsJob = viewModelScope.launch {
            getSlots(route.id, today, sport).collect { list ->
                _uiState.update { s ->
                    (s as? ArenaDetailUiState.Content)?.copy(todaySlots = list.groupBy({ it.quadra.id }, { it.slot })) ?: s
                }
            }
        }
    }
}
