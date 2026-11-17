package br.com.sportconnect.core.data.fake

import br.com.sportconnect.core.domain.repository.ArenaRepository
import br.com.sportconnect.core.model.Arena
import br.com.sportconnect.core.model.FusoSaoPaulo
import br.com.sportconnect.core.model.Quadra
import br.com.sportconnect.core.model.Repasse
import br.com.sportconnect.core.model.Slot
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toLocalDateTime

/** Repositorio de arenas em memoria, populado com [DemoData]. */
class FakeArenaRepository(seed: DemoData) : ArenaRepository {

    private val arenas = MutableStateFlow(seed.arenas)
    private val quadras = seed.quadras
    private val _slots = MutableStateFlow(seed.slots)
    private val repasses = MutableStateFlow(seed.repasses)

    val slotsSnapshot: StateFlow<Map<String, Slot>> = _slots.asStateFlow()

    override fun observeArenas(): Flow<List<Arena>> = arenas
    override suspend fun arenas(): List<Arena> = arenas.value
    override suspend fun arena(id: String): Arena? = arenas.value.firstOrNull { it.id == id }
    override suspend fun quadras(arenaId: String): List<Quadra> = quadras.filter { it.arenaId == arenaId }
    override suspend fun quadra(id: String): Quadra? = quadras.firstOrNull { it.id == id }

    override fun observeSlots(arenaId: String, data: LocalDate): Flow<List<Slot>> = _slots.map { mapa ->
        mapa.values.filter { it.arenaId == arenaId && it.inicio.toLocalDateTime(FusoSaoPaulo).date == data }
    }.distinctUntilChanged()

    override suspend fun slot(id: String): Slot? = _slots.value[id]

    override suspend fun atualizarSlot(slotId: String, transform: (Slot) -> Slot): Slot? {
        var resultado: Slot? = null
        _slots.update { mapa ->
            val atual = mapa[slotId] ?: return@update mapa
            val novo = transform(atual)
            resultado = novo
            mapa + (slotId to novo)
        }
        return resultado
    }

    override fun observeRepasses(arenaId: String): Flow<List<Repasse>> =
        repasses.map { lista -> lista.filter { it.arenaId == arenaId } }.distinctUntilChanged()

    override suspend fun salvarRepasse(repasse: Repasse) {
        repasses.update { lista -> lista.filterNot { it.id == repasse.id } + repasse }
    }
}
