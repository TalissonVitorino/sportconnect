package br.com.sportconnect.feature.arenaadmin.courts

import br.com.sportconnect.core.model.Quadra
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate

/** Funcionamento e faixas de preco de uma quadra (10 §2.20 "Funcionamento"/"Precos"). */
data class CourtSchedule(
    val days: Set<DayOfWeek> = DayOfWeek.entries.toSet(),
    val openHour: Int = 7,
    val closeHour: Int = 23,
    val durationMinutes: Int = 60,
    val peakStartHour: Int = 18,
    val peakEndHour: Int = 23,
)

/**
 * Rascunhos/edicoes de quadras feitos no modo arena. O [br.com.sportconnect.core.domain.repository.ArenaRepository]
 * ainda nao expoe escrita de quadras; ate o backend existir, as edicoes vivem aqui (escopo do app) e a
 * publicacao atualiza o preco dos horarios livres ja existentes via `atualizarSlot`.
 */
class ArenaCourtStore {
    private val _overrides = MutableStateFlow<Map<String, Quadra>>(emptyMap())
    val overrides: StateFlow<Map<String, Quadra>> = _overrides.asStateFlow()

    private val _schedules = MutableStateFlow<Map<String, CourtSchedule>>(emptyMap())
    val schedules: StateFlow<Map<String, CourtSchedule>> = _schedules.asStateFlow()

    /** Ultimo dia com horarios publicados por quadra (resolve o alerta "sem horarios a partir de"). */
    private val _publishedUntil = MutableStateFlow<Map<String, LocalDate>>(emptyMap())
    val publishedUntil: StateFlow<Map<String, LocalDate>> = _publishedUntil.asStateFlow()

    private var seq = 0

    fun newId(arenaId: String): String = "q-new-$arenaId-${++seq}"

    fun save(quadra: Quadra, schedule: CourtSchedule) {
        _overrides.update { it + (quadra.id to quadra) }
        _schedules.update { it + (quadra.id to schedule) }
    }

    fun markPublished(courtId: String, until: LocalDate) {
        _publishedUntil.update { it + (courtId to until) }
    }

    fun schedule(courtId: String): CourtSchedule? = _schedules.value[courtId]

    /** Quadras do repositorio com as edicoes aplicadas + quadras novas da arena. */
    fun merge(arenaId: String, base: List<Quadra>, overrides: Map<String, Quadra> = _overrides.value): List<Quadra> {
        val ids = base.map { it.id }.toSet()
        return base.map { overrides[it.id] ?: it } + overrides.values.filter { it.arenaId == arenaId && it.id !in ids }
    }
}
