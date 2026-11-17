package br.com.sportconnect.core.domain.repository

import br.com.sportconnect.core.model.Arena
import br.com.sportconnect.core.model.Quadra
import br.com.sportconnect.core.model.Repasse
import br.com.sportconnect.core.model.Slot
import br.com.sportconnect.core.model.SlotStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

interface ArenaRepository {
    fun observeArenas(): Flow<List<Arena>>
    suspend fun arenas(): List<Arena>
    suspend fun arena(id: String): Arena?
    suspend fun quadras(arenaId: String): List<Quadra>
    suspend fun quadra(id: String): Quadra?

    /** Slots da arena no dia [data] (fuso de Sao Paulo), em tempo real. */
    fun observeSlots(arenaId: String, data: LocalDate): Flow<List<Slot>>
    suspend fun slot(id: String): Slot?

    /** Atualiza status/jogo do slot (usado pelo servico de reservas). */
    suspend fun atualizarSlot(slotId: String, transform: (Slot) -> Slot): Slot?
    suspend fun alterarStatusSlot(slotId: String, status: SlotStatus, jogoId: String? = null): Slot? =
        atualizarSlot(slotId) { it.copy(status = status, jogoId = jogoId ?: it.jogoId) }

    fun observeRepasses(arenaId: String): Flow<List<Repasse>>
    suspend fun salvarRepasse(repasse: Repasse)
}
