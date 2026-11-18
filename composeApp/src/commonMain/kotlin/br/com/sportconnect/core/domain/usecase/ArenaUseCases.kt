package br.com.sportconnect.core.domain.usecase

import br.com.sportconnect.core.domain.repository.AnalyticsEvents
import br.com.sportconnect.core.domain.repository.AnalyticsTracker
import br.com.sportconnect.core.domain.repository.ArenaRepository
import br.com.sportconnect.core.model.Arena
import br.com.sportconnect.core.model.FusoSaoPaulo
import br.com.sportconnect.core.model.GeoPonto
import br.com.sportconnect.core.model.Money
import br.com.sportconnect.core.model.Quadra
import br.com.sportconnect.core.model.Slot
import br.com.sportconnect.core.model.Sport
import br.com.sportconnect.core.model.StatusArena
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toLocalDateTime
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.time.Clock

data class ArenaProxima(
    val arena: Arena,
    val distanciaKm: Double,
    val precoMinimoHora: Money,
    val esportes: Set<Sport>,
    val quantidadeQuadras: Int,
) {
    /** "1,2 km" */
    val distanciaFormatada: String
        get() = ((distanciaKm * 10).roundToInt() / 10.0).toString().replace('.', ',') + " km"
}

data class ArenaDetalhe(
    val arena: Arena,
    val quadras: List<Quadra>,
    val esportes: Set<Sport>,
    val precoMinimoHora: Money,
    val precoMaximoHora: Money,
)

data class SlotDisponivel(
    val slot: Slot,
    val quadra: Quadra,
    val disponivel: Boolean,
)

object Geo {
    /** Av. Paulista (MASP) — origem padrao quando nao ha localizacao. */
    val CENTRO_SP = GeoPonto(-23.5614, -46.6559)

    fun distanciaKm(a: GeoPonto, b: GeoPonto): Double {
        val r = 6371.0
        val dLat = (b.latitude - a.latitude) * PI / 180
        val dLon = (b.longitude - a.longitude) * PI / 180
        val la1 = a.latitude * PI / 180
        val la2 = b.latitude * PI / 180
        val h = sin(dLat / 2) * sin(dLat / 2) + cos(la1) * cos(la2) * sin(dLon / 2) * sin(dLon / 2)
        return 2 * r * atan2(sqrt(h), sqrt(1 - h))
    }
}

/** Busca de arenas proximas (US-020), ordenadas por distancia. */
class GetArenasNearbyUseCase(
    private val arenas: ArenaRepository,
    private val analytics: AnalyticsTracker,
) {
    suspend operator fun invoke(
        origem: GeoPonto? = null,
        esporte: Sport? = null,
        raioKm: Double? = null,
    ): List<ArenaProxima> {
        val ref = origem ?: Geo.CENTRO_SP
        val resultado = arenas.arenas().mapNotNull { arena ->
            // T9 (13/US-012): so arena publicada (ATIVA) aparece na busca.
            if (arena.status != StatusArena.ATIVA) return@mapNotNull null
            val quadras = arenas.quadras(arena.id).filter { it.ativa }
            val esportes = quadras.flatMap { q -> q.esportes.map { it.esporte } }.toSet()
            if (esporte != null && esporte !in esportes) return@mapNotNull null
            val relevantes = if (esporte != null) quadras.filter { it.suporta(esporte) } else quadras
            ArenaProxima(
                arena = arena,
                distanciaKm = Geo.distanciaKm(ref, arena.localizacao),
                precoMinimoHora = relevantes.minOfOrNull { it.precoHora } ?: Money.ZERO,
                esportes = esportes,
                quantidadeQuadras = quadras.size,
            )
        }.filter { raioKm == null || it.distanciaKm <= raioKm }.sortedBy { it.distanciaKm }
        analytics.track(
            AnalyticsEvents.BUSCA_REALIZADA,
            mapOf("cidade" to "São Paulo", "esporte" to esporte?.name, "qtd_resultados" to resultado.size, "localizacao_permitida" to (origem != null)),
        )
        return resultado
    }
}

class GetArenaDetailUseCase(private val arenas: ArenaRepository) {
    suspend operator fun invoke(arenaId: String): ArenaDetalhe? {
        val arena = arenas.arena(arenaId) ?: return null
        val quadras = arenas.quadras(arenaId).filter { it.ativa }
        return ArenaDetalhe(
            arena = arena,
            quadras = quadras,
            esportes = quadras.flatMap { q -> q.esportes.map { it.esporte } }.toSet(),
            precoMinimoHora = quadras.minOfOrNull { it.precoHora } ?: Money.ZERO,
            precoMaximoHora = quadras.maxOfOrNull { it.precoHoraPico } ?: Money.ZERO,
        )
    }
}

/**
 * Horarios do dia (US-020) em tempo real. Por padrao so os disponiveis (LIVRE ou checkout expirado,
 * no futuro, quadra compativel com o esporte). BLOQUEADO nunca e oferecido (RN-33).
 */
class GetAvailableSlotsUseCase(
    private val arenas: ArenaRepository,
    private val clock: Clock,
) {
    operator fun invoke(
        arenaId: String,
        data: LocalDate,
        esporte: Sport? = null,
        incluirIndisponiveis: Boolean = false,
    ): Flow<List<SlotDisponivel>> = arenas.observeSlots(arenaId, data).map { slots ->
        val quadras = arenas.quadras(arenaId).associateBy { it.id }
        val agora = clock.now()
        slots.mapNotNull { slot ->
            val quadra = quadras[slot.quadraId] ?: return@mapNotNull null
            if (!quadra.ativa) return@mapNotNull null
            if (esporte != null && !quadra.suporta(esporte)) return@mapNotNull null
            if (slot.inicio <= agora) return@mapNotNull null
            SlotDisponivel(slot, quadra, slot.disponivelEm(agora))
        }.filter { incluirIndisponiveis || it.disponivel }
            .sortedWith(compareBy({ it.slot.inicio }, { it.quadra.nome }))
    }

    fun hoje(): LocalDate = clock.now().toLocalDateTime(FusoSaoPaulo).date
}
