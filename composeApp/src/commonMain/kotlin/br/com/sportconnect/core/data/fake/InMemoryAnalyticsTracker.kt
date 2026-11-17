package br.com.sportconnect.core.data.fake

import br.com.sportconnect.core.domain.repository.AnalyticsTracker
import br.com.sportconnect.core.domain.repository.EventoAnalytics
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** Guarda os eventos em memoria (ultimos [limite]); util para debug e testes. */
class InMemoryAnalyticsTracker(private val limite: Int = 500) : AnalyticsTracker {
    private val _eventos = MutableStateFlow<List<EventoAnalytics>>(emptyList())
    val eventos: StateFlow<List<EventoAnalytics>> = _eventos.asStateFlow()

    override fun track(evento: String, propriedades: Map<String, Any?>) {
        _eventos.update { (it + EventoAnalytics(evento, propriedades)).takeLast(limite) }
    }

    fun nomes(): List<String> = _eventos.value.map { it.nome }
}
