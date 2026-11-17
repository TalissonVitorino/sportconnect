package br.com.sportconnect.feature.arenaadmin.common

import br.com.sportconnect.core.domain.PayoutCalculator
import br.com.sportconnect.core.domain.usecase.ItemAgenda
import br.com.sportconnect.core.model.Jogo
import br.com.sportconnect.core.model.Money
import br.com.sportconnect.core.model.Plano
import br.com.sportconnect.core.model.Quadra
import br.com.sportconnect.core.model.Slot
import br.com.sportconnect.core.model.SlotStatus
import br.com.sportconnect.core.model.Sport
import br.com.sportconnect.core.model.StatusCota
import br.com.sportconnect.core.model.StatusReserva
import br.com.sportconnect.core.model.TipoCota
import br.com.sportconnect.feature.arenaadmin.common.ArenaFormat.local
import kotlin.time.Instant

/** Estado visual de um horario na agenda da arena (10 §2.17 "Bloco da agenda"). */
enum class AgendaCellStatus { Free, Confirmed, InProgress, Hold, Checkout, Blocked, External, Closed }

/** Uma celula (quadra x hora) da agenda. [slotId] null = horario sem oferta (Fechado). */
data class AgendaCellUi(
    val courtId: String,
    val hour: Int,
    val status: AgendaCellStatus,
    val slotId: String? = null,
    val sport: Sport? = null,
    /** Texto da linha 2 ("Bruno L." / motivo do bloqueio / "Mensalista"). */
    val detail: String? = null,
    val paid: Int = 0,
    val total: Int = 0,
    /** "20h" — fim do hold da pre-reserva. */
    val holdUntil: String? = null,
    val jogoId: String? = null,
    val organizer: String? = null,
    val startLabel: String = ArenaFormat.hour(hour),
    val rangeLabel: String = "${ArenaFormat.hour(hour)}–${ArenaFormat.hour((hour + 1) % 24)}",
) {
    /** Tem reserva (da plataforma ou externa) — tocar abre o detalhe. */
    val isBooking: Boolean
        get() = status == AgendaCellStatus.Confirmed || status == AgendaCellStatus.InProgress ||
            status == AgendaCellStatus.Hold || status == AgendaCellStatus.Checkout || status == AgendaCellStatus.External
}

data class CourtAgendaUi(
    val courtId: String,
    val name: String,
    val mainSport: Sport,
    val sports: List<Sport>,
    /** Uma celula por hora de [ArenaAgenda.hours], na mesma ordem. */
    val cells: List<AgendaCellUi>,
)

data class ArenaAgenda(
    val hours: List<Int>,
    val courts: List<CourtAgendaUi>,
) {
    fun cell(slotId: String): AgendaCellUi? = courts.asSequence().flatMap { it.cells.asSequence() }.firstOrNull { it.slotId == slotId }

    companion object {
        val EMPTY = ArenaAgenda(emptyList(), emptyList())
        const val DEFAULT_FIRST_HOUR = 7
        const val DEFAULT_LAST_HOUR = 23
    }
}

object AgendaMapper {

    fun status(slot: Slot, jogo: Jogo?, now: Instant): AgendaCellStatus = when (slot.status) {
        SlotStatus.LIVRE -> AgendaCellStatus.Free
        SlotStatus.BLOQUEADO -> AgendaCellStatus.Blocked
        SlotStatus.EM_CHECKOUT -> if (slot.checkoutAte != null && slot.checkoutAte <= now) AgendaCellStatus.Free else AgendaCellStatus.Checkout
        SlotStatus.PRE_RESERVADO -> AgendaCellStatus.Hold
        SlotStatus.RESERVADO -> when {
            jogo == null && slot.reservaExterna != null -> if (slot.inicio <= now && now < slot.fim) AgendaCellStatus.InProgress else AgendaCellStatus.External
            jogo?.status == StatusReserva.EM_ANDAMENTO -> AgendaCellStatus.InProgress
            slot.inicio <= now && now < slot.fim -> AgendaCellStatus.InProgress
            else -> AgendaCellStatus.Confirmed
        }
    }

    fun cell(item: ItemAgenda, now: Instant): AgendaCellUi {
        val slot = item.slot
        val jogo = item.jogo
        val st = status(slot, jogo, now)
        val hour = slot.inicio.local().hour
        return AgendaCellUi(
            courtId = item.quadra.id,
            hour = hour,
            status = st,
            slotId = slot.id,
            sport = jogo?.esporte ?: item.quadra.esportes.firstOrNull()?.esporte?.takeIf { st.hasBooking() },
            detail = when {
                st == AgendaCellStatus.Blocked -> slot.motivoBloqueio
                jogo != null -> ArenaFormat.shortName(jogo.organizadorNome)
                slot.reservaExterna != null -> slot.reservaExterna
                else -> null
            },
            paid = jogo?.cotasPagas ?: 0,
            total = jogo?.numeroVagas ?: 0,
            holdUntil = jogo?.reserva?.prazoHold?.let { ArenaFormat.hour(it) },
            jogoId = jogo?.id,
            organizer = jogo?.organizadorNome,
            rangeLabel = ArenaFormat.range(slot.inicio, slot.fim),
        )
    }

    private fun AgendaCellStatus.hasBooking() = this != AgendaCellStatus.Free && this != AgendaCellStatus.Blocked && this != AgendaCellStatus.Closed

    /**
     * Monta a grade do dia: colunas = quadras (ordem do repositorio), linhas = horas.
     * Horas sem slot numa quadra viram [AgendaCellStatus.Closed].
     */
    fun agenda(quadras: List<Quadra>, items: List<ItemAgenda>, now: Instant): ArenaAgenda {
        if (quadras.isEmpty()) return ArenaAgenda.EMPTY
        val hoursWithSlots = items.map { it.slot.inicio.local().hour }
        val first = minOf(hoursWithSlots.minOrNull() ?: ArenaAgenda.DEFAULT_FIRST_HOUR, ArenaAgenda.DEFAULT_FIRST_HOUR)
        val last = maxOf(hoursWithSlots.maxOrNull() ?: ArenaAgenda.DEFAULT_LAST_HOUR, ArenaAgenda.DEFAULT_LAST_HOUR)
        val hours = (first..last).toList()
        val byCourt = items.groupBy { it.quadra.id }
        val courts = quadras.map { q ->
            val cellsByHour = byCourt[q.id].orEmpty().associate { it.slot.inicio.local().hour to cell(it, now) }
            CourtAgendaUi(
                courtId = q.id,
                name = q.nome,
                mainSport = q.esportes.firstOrNull()?.esporte ?: Sport.FUTEBOL_SOCIETY,
                sports = q.esportes.map { it.esporte },
                cells = hours.map { h -> cellsByHour[h] ?: AgendaCellUi(q.id, h, AgendaCellStatus.Closed) },
            )
        }
        return ArenaAgenda(hours, courts)
    }
}

// ------------------------------------------------------------------ detalhe da reserva

data class ParticipantUi(val name: String, val paid: Boolean, val organizer: Boolean)

/** Dados do detalhe da reserva (tela `arena-admin/booking/{id}` e painel lateral do Painel). */
data class BookingDetailUi(
    val slotId: String,
    val jogoId: String?,
    val cellStatus: AgendaCellStatus,
    val gameStatus: StatusReserva?,
    val sport: Sport?,
    val courtName: String,
    val dateLabel: String,
    val rangeLabel: String,
    val organizer: String?,
    val organizerPhone: String? = null,
    val paid: Int,
    val total: Int,
    val collected: Money,
    val holdUntil: String?,
    val gross: Money,
    val fee: Money,
    val net: Money,
    val feePercent: Int,
    val externalLabel: String?,
    val blockedReason: String?,
    val participants: List<ParticipantUi>,
    /** Arena pode cancelar (RN-24): reserva da plataforma nao terminal. */
    val cancellable: Boolean,
)

object BookingMapper {
    fun detail(
        slot: Slot,
        quadra: Quadra,
        jogo: Jogo?,
        plano: Plano,
        payout: PayoutCalculator,
        now: Instant,
        organizerPhone: String? = null,
    ): BookingDetailUi {
        val gross = jogo?.valorTotal ?: slot.preco
        val calc = payout.calcular(gross, plano)
        val cellStatus = when {
            jogo != null && jogo.status.terminal -> AgendaCellStatus.Free
            else -> AgendaMapper.status(slot, jogo, now)
        }
        val participants = jogo?.reserva?.cotas.orEmpty()
            .filter { it.tipo == TipoCota.PADRAO && it.status != StatusCota.REEMBOLSADA }
            .mapNotNull { c ->
                val nome = c.atletaNome ?: return@mapNotNull null
                ParticipantUi(nome, c.status == StatusCota.PAGA, c.organizador)
            }
            .sortedWith(compareByDescending<ParticipantUi> { it.organizer }.thenByDescending { it.paid })
        val local = slot.inicio.local()
        return BookingDetailUi(
            slotId = slot.id,
            jogoId = jogo?.id,
            cellStatus = cellStatus,
            gameStatus = jogo?.status,
            sport = jogo?.esporte ?: quadra.esportes.firstOrNull()?.esporte,
            courtName = quadra.nome,
            dateLabel = ArenaFormat.dayLabel(local.date),
            rangeLabel = ArenaFormat.range(slot.inicio, slot.fim),
            organizer = jogo?.organizadorNome,
            organizerPhone = organizerPhone,
            paid = jogo?.cotasPagas ?: 0,
            total = jogo?.numeroVagas ?: 0,
            collected = jogo?.valorArrecadado ?: Money.ZERO,
            holdUntil = jogo?.reserva?.prazoHold?.let { ArenaFormat.hour(it) },
            gross = calc.bruto,
            fee = calc.comissao,
            net = calc.liquido,
            feePercent = calc.percentual,
            externalLabel = slot.reservaExterna.takeIf { jogo == null },
            blockedReason = slot.motivoBloqueio.takeIf { slot.status == SlotStatus.BLOQUEADO },
            participants = participants,
            cancellable = jogo != null && !jogo.status.terminal &&
                (jogo.status == StatusReserva.AGUARDANDO_PAGAMENTOS || jogo.status == StatusReserva.CONFIRMADO),
        )
    }
}
