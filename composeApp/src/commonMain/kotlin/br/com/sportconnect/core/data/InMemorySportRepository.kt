package br.com.sportconnect.core.data

import br.com.sportconnect.core.domain.SportRepository
import br.com.sportconnect.core.model.Sport

/** Implementacao provisoria em memoria; sera substituida por fonte remota (Ktor). */
class InMemorySportRepository : SportRepository {
    override suspend fun availableSports(): List<Sport> = Sport.entries
}
