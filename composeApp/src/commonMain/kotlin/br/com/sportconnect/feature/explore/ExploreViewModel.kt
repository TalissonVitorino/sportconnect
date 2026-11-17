package br.com.sportconnect.feature.explore

import br.com.sportconnect.core.data.local.AppPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.sportconnect.core.domain.usecase.ArenaProxima
import br.com.sportconnect.core.domain.usecase.GetArenaDetailUseCase
import br.com.sportconnect.core.domain.usecase.GetArenasNearbyUseCase
import br.com.sportconnect.core.domain.usecase.GetAvailableSlotsUseCase
import br.com.sportconnect.core.model.FusoSaoPaulo
import br.com.sportconnect.core.model.Quadra
import br.com.sportconnect.core.model.Slot
import br.com.sportconnect.core.model.Sport
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

/** Uma quadra com horarios livres no dia filtrado (card da busca). */
data class CourtResult(
    val arena: ArenaProxima,
    val quadra: Quadra,
    val freeSlots: List<Slot>,
)

data class ExploreUiState(
    val query: String = "",
    val sport: Sport? = null,
    val date: LocalDate,
    val today: LocalDate,
    val coveredOnly: Boolean = false,
    val loading: Boolean = true,
    val error: Boolean = false,
    val results: List<CourtResult> = emptyList(),
) {
    val days: List<LocalDate> get() = (0..6).map { today.plus(DatePeriod(days = it)) }
}

class ExploreViewModel(
    private val nearby: GetArenasNearbyUseCase,
    private val arenaDetail: GetArenaDetailUseCase,
    private val slots: GetAvailableSlotsUseCase,
    private val clock: Clock,
    private val prefs: AppPreferences? = null,
) : ViewModel() {
    /** "Meus esportes" (onboarding/perfil): quadras desses esportes aparecem primeiro (C7/B10). */
    private val favorites: Set<Sport> get() = prefs?.favoriteSports.orEmpty().mapNotNull { n -> Sport.entries.firstOrNull { it.name == n } }.toSet()
    private val today = clock.now().toLocalDateTime(FusoSaoPaulo).date
    private val _uiState = MutableStateFlow(ExploreUiState(date = today, today = today))
    val uiState: StateFlow<ExploreUiState> = _uiState.asStateFlow()
    private var job: Job? = null

    init {
        search()
    }

    fun setQuery(q: String) {
        _uiState.update { it.copy(query = q) }
        search()
    }

    fun setSport(s: Sport?) {
        _uiState.update { it.copy(sport = s) }
        search()
    }

    fun setDate(d: LocalDate) {
        _uiState.update { it.copy(date = d) }
        search()
    }

    fun toggleCovered() {
        _uiState.update { it.copy(coveredOnly = !it.coveredOnly) }
        search()
    }

    fun clearFilters() {
        _uiState.update { it.copy(query = "", sport = null, coveredOnly = false, date = today) }
        search()
    }

    fun retry() = search()

    private fun search() {
        job?.cancel()
        val s = _uiState.value
        _uiState.update { it.copy(loading = true, error = false) }
        job = viewModelScope.launch {
            runCatching {
                val arenas = nearby(esporte = s.sport)
                val q = s.query.trim().lowercase()
                arenas.filter {
                    q.isEmpty() || it.arena.nome.lowercase().contains(q) || it.arena.endereco.bairro.lowercase().contains(q) ||
                        it.arena.endereco.cidade.lowercase().contains(q)
                }.flatMap { a ->
                    val detail = arenaDetail(a.arena.id) ?: return@flatMap emptyList()
                    val free = slots(a.arena.id, s.date, s.sport).first()
                    detail.quadras
                        .filter { s.sport == null || it.suporta(s.sport) }
                        .filter { !s.coveredOnly || it.coberta }
                        .map { quadra -> CourtResult(a, quadra, free.filter { it.quadra.id == quadra.id }.map { it.slot }) }
                        .filter { it.freeSlots.isNotEmpty() }
                }
            }.fold(
                onSuccess = { list ->
                    val fav = favorites
                    // Ordenacao estavel: favoritos primeiro, o resto mantem a ordem (distancia).
                    val ordered = if (fav.isEmpty()) list else list.sortedByDescending { r -> fav.any { r.quadra.suporta(it) } }
                    _uiState.update { it.copy(loading = false, results = ordered) }
                },
                onFailure = { _uiState.update { it.copy(loading = false, error = true) } },
            )
        }
    }
}
