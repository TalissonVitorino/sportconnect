package br.com.sportconnect.core.domain.usecase

import br.com.sportconnect.core.domain.repository.AnalyticsEvents
import br.com.sportconnect.core.domain.repository.AnalyticsTracker
import br.com.sportconnect.core.domain.repository.ArenaRepository
import br.com.sportconnect.core.domain.repository.GameRepository
import br.com.sportconnect.core.model.Arena
import br.com.sportconnect.core.model.Jogo
import br.com.sportconnect.core.model.Money
import br.com.sportconnect.core.model.Quadra
import br.com.sportconnect.core.model.Repasse
import br.com.sportconnect.core.model.Slot
import br.com.sportconnect.core.model.SlotStatus
import br.com.sportconnect.core.model.StatusRepasse
import br.com.sportconnect.core.model.StatusReserva
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.datetime.LocalDate

data class ItemAgenda(
    val slot: Slot,
    val quadra: Quadra,
    /** Jogo da plataforma que ocupa o slot (null se livre, bloqueado ou reserva externa). */
    val jogo: Jogo?,
) {
    val status: SlotStatus get() = slot.status
}

data class PainelArena(
    val arena: Arena,
    val data: LocalDate,
    val agenda: List<ItemAgenda>,
    /** Slots ocupados (pre-reservado, reservado, checkout) ÷ slots ofertados (exclui bloqueados), 0..100. */
    val ocupacaoPct: Int,
    val confirmados: Int,
    val preReservas: Int,
    val livres: Int,
    val bloqueados: Int,
    /** Soma de V dos jogos confirmados do dia (RN-30 bruto). */
    val receitaPrevistaDia: Money,
    val repasses: List<Repasse>,
    val repasseAgendado: Money,
    val repasseLiquidado: Money,
) {
    val agendaPorQuadra: Map<Quadra, List<ItemAgenda>> get() = agenda.groupBy { it.quadra }
}

/** Painel da arena (US-060/062): agenda do dia, ocupacao e repasses, em tempo real. */
@OptIn(ExperimentalCoroutinesApi::class)
class ArenaDashboardUseCase(
    private val arenas: ArenaRepository,
    private val games: GameRepository,
    private val analytics: AnalyticsTracker,
) {
    operator fun invoke(arenaId: String, data: LocalDate): Flow<PainelArena> = flow<Pair<Arena, Map<String, Quadra>>?> {
        val arena = arenas.arena(arenaId)
        if (arena == null) {
            emit(null)
            return@flow
        }
        analytics.track(AnalyticsEvents.AGENDA_ARENA_VISUALIZADA, mapOf("arena_id" to arenaId, "visao" to "dia"))
        val quadras = arenas.quadras(arenaId).associateBy { it.id }
        emit(arena to quadras)
    }.flatMapLatest { ctx ->
        if (ctx == null) return@flatMapLatest emptyFlow()
        val (arena, quadras) = ctx
        combine(arenas.observeSlots(arenaId, data), games.observeGames(), arenas.observeRepasses(arenaId)) { slots, jogos, repasses ->
            val porId = jogos.associateBy { it.id }
            val agenda = slots.mapNotNull { s ->
                val q = quadras[s.quadraId] ?: return@mapNotNull null
                ItemAgenda(s, q, s.jogoId?.let { porId[it] })
            }.sortedWith(compareBy({ it.quadra.nome }, { it.slot.inicio }))
            val bloqueados = agenda.count { it.status == SlotStatus.BLOQUEADO }
            val ocupados = agenda.count { it.status == SlotStatus.RESERVADO || it.status == SlotStatus.PRE_RESERVADO || it.status == SlotStatus.EM_CHECKOUT }
            val ofertados = agenda.size - bloqueados
            val confirmadosJogos = agenda.mapNotNull { it.jogo }.filter {
                it.status == StatusReserva.CONFIRMADO || it.status == StatusReserva.EM_ANDAMENTO || it.status == StatusReserva.CONCLUIDO
            }
            PainelArena(
                arena = arena,
                data = data,
                agenda = agenda,
                ocupacaoPct = if (ofertados == 0) 0 else ocupados * 100 / ofertados,
                confirmados = agenda.count { it.status == SlotStatus.RESERVADO },
                preReservas = agenda.count { it.status == SlotStatus.PRE_RESERVADO },
                livres = agenda.count { it.status == SlotStatus.LIVRE },
                bloqueados = bloqueados,
                receitaPrevistaDia = Money(confirmadosJogos.sumOf { it.valorTotal.cents }),
                repasses = repasses.sortedByDescending { it.dataPrevista },
                repasseAgendado = Money(repasses.filter { it.status == StatusRepasse.AGENDADO }.sumOf { it.valorLiquido.cents }),
                repasseLiquidado = Money(repasses.filter { it.status == StatusRepasse.LIQUIDADO }.sumOf { it.valorLiquido.cents }),
            )
        }
    }
}
