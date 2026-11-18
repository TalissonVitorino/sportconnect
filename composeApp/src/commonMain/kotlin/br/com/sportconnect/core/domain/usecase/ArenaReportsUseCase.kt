package br.com.sportconnect.core.domain.usecase

import br.com.sportconnect.core.domain.repository.ArenaRepository
import br.com.sportconnect.core.domain.repository.GameRepository
import br.com.sportconnect.core.model.FusoSaoPaulo
import br.com.sportconnect.core.model.Money
import br.com.sportconnect.core.model.SlotStatus
import br.com.sportconnect.core.model.StatusReserva
import kotlinx.coroutines.flow.first
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

data class CourtReport(val courtId: String, val courtName: String, val offered: Int, val booked: Int) {
    /** Ocupacao = horarios reservados (confirmados ou pre-reservados) / horarios ofertados (nao bloqueados). */
    val occupancyPct: Int get() = if (offered == 0) 0 else (booked * 100 / offered)
}

data class ArenaReport(
    val days: Int,
    val courts: List<CourtReport>,
    val netRevenue: Money,
    val cancelled: Int,
    val expired: Int,
) {
    val occupancyPct: Int get() {
        val offered = courts.sumOf { it.offered }
        return if (offered == 0) 0 else courts.sumOf { it.booked } * 100 / offered
    }
}

/**
 * Relatorio simples da arena (E11, US-063): ocupacao por quadra, receita liquida (repasses) e jogos
 * cancelados/expirados nos ultimos [days] dias, ate hoje.
 */
class ArenaReportsUseCase(
    private val arenas: ArenaRepository,
    private val games: GameRepository,
    private val clock: Clock,
) {
    suspend operator fun invoke(arenaId: String, days: Int): ArenaReport {
        val today = clock.now().toLocalDateTime(FusoSaoPaulo).date
        val from = today.minus(DatePeriod(days = days - 1))
        val dates = generateSequence(from) { it.plus(DatePeriod(days = 1)) }.takeWhile { it <= today }.toList()
        val courts = arenas.quadras(arenaId)
        val slots = dates.flatMap { arenas.observeSlots(arenaId, it).first() }
        val booked = setOf(SlotStatus.RESERVADO, SlotStatus.PRE_RESERVADO, SlotStatus.EM_CHECKOUT)
        val courtReports = courts.map { q ->
            val s = slots.filter { it.quadraId == q.id }
            CourtReport(q.id, q.nome, offered = s.count { it.status != SlotStatus.BLOQUEADO }, booked = s.count { it.status in booked })
        }
        val revenue = arenas.observeRepasses(arenaId).first()
            .filter { it.dataPrevista in from..today }
            .fold(Money.ZERO) { acc, r -> acc + r.valorLiquido }
        val inPeriod = games.observeGames().first().filter { j ->
            j.arenaId == arenaId && j.inicio.toLocalDateTime(FusoSaoPaulo).date.let { d: LocalDate -> d in from..today }
        }
        return ArenaReport(
            days = days,
            courts = courtReports,
            netRevenue = revenue,
            cancelled = inPeriod.count { it.status == StatusReserva.CANCELADO },
            expired = inPeriod.count { it.status == StatusReserva.EXPIRADO },
        )
    }
}
