package br.com.sportconnect.core.model

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration
import kotlin.time.Instant

/**
 * Formatacao pt-BR do 07 §2.3, sem depender de `java.text` (roda em todos os alvos KMP):
 * - data: "qui, 21 nov" · data + hora: "qui, 21 nov · 20h" · hora: "20h" / "18h40" · faixa: "20h–21h";
 * - relativa: "hoje, 18h" / "amanhã, 18h" / "sex, 22 nov · 18h";
 * - duracao: "1h 45min" · "28min" · segundos so no ultimo minuto ("45s");
 * - dinheiro: `Money.format()` ("R$ 1.234,56").
 * Fuso: [FusoSaoPaulo].
 */
object PtBrFormat {
    private val weekdays = mapOf(
        DayOfWeek.MONDAY to "seg", DayOfWeek.TUESDAY to "ter", DayOfWeek.WEDNESDAY to "qua",
        DayOfWeek.THURSDAY to "qui", DayOfWeek.FRIDAY to "sex", DayOfWeek.SATURDAY to "sáb", DayOfWeek.SUNDAY to "dom",
    )
    private val months = listOf("jan", "fev", "mar", "abr", "mai", "jun", "jul", "ago", "set", "out", "nov", "dez")
    private val monthsLong = listOf(
        "janeiro", "fevereiro", "março", "abril", "maio", "junho", "julho", "agosto", "setembro", "outubro", "novembro", "dezembro",
    )

    fun local(instant: Instant, zone: TimeZone = FusoSaoPaulo): LocalDateTime = instant.toLocalDateTime(zone)
    fun date(instant: Instant, zone: TimeZone = FusoSaoPaulo): LocalDate = local(instant, zone).date

    /** "qui" */
    fun weekdayShort(date: LocalDate): String = weekdays.getValue(date.dayOfWeek)

    /** "21 nov" */
    fun dayMonth(date: LocalDate): String = "${date.day} ${months[date.month.ordinal]}"

    /** "novembro de 2026" (cabecalho de lista por mes). */
    fun monthYear(date: LocalDate): String = "${monthsLong[date.month.ordinal]} de ${date.year}"

    /** "qui, 21 nov" */
    fun date(date: LocalDate): String = "${weekdayShort(date)}, ${dayMonth(date)}"

    /** "20h" ou "18h40" */
    fun hour(instant: Instant, zone: TimeZone = FusoSaoPaulo): String {
        val t = local(instant, zone)
        return hour(t.hour, t.minute)
    }

    fun hour(h: Int, m: Int): String = if (m == 0) "${h}h" else "${h}h${m.toString().padStart(2, '0')}"

    /** "14h32" (hora com minutos sempre, usada no banner offline). */
    fun clock(instant: Instant, zone: TimeZone = FusoSaoPaulo): String {
        val t = local(instant, zone)
        return "${t.hour}h${t.minute.toString().padStart(2, '0')}"
    }

    /** "qui, 21 nov · 20h" */
    fun dateTime(instant: Instant, zone: TimeZone = FusoSaoPaulo): String = "${date(date(instant, zone))} · ${hour(instant, zone)}"

    /** "20h–21h" */
    fun timeRange(start: Instant, end: Instant, zone: TimeZone = FusoSaoPaulo): String = "${hour(start, zone)}–${hour(end, zone)}"

    /** "qui, 21 nov · 20h–21h" */
    fun dateTimeRange(start: Instant, end: Instant, zone: TimeZone = FusoSaoPaulo): String =
        "${date(date(start, zone))} · ${timeRange(start, end, zone)}"

    /** "hoje, 18h" / "amanhã, 18h" / "sex, 22 nov · 18h" */
    fun relative(instant: Instant, now: Instant, zone: TimeZone = FusoSaoPaulo): String {
        val d = date(instant, zone)
        val today = date(now, zone)
        return when (d.toEpochDays() - today.toEpochDays()) {
            0L -> "hoje, ${hour(instant, zone)}"
            1L -> "amanhã, ${hour(instant, zone)}"
            else -> dateTime(instant, zone)
        }
    }

    /** "hoje" / "amanhã" / "sáb, 23 nov" (rotulo de dia em filtros). */
    fun relativeDay(date: LocalDate, today: LocalDate): String = when (date.toEpochDays() - today.toEpochDays()) {
        0L -> "hoje"
        1L -> "amanhã"
        else -> date(date)
    }

    /** T2 (doc 13): rotulo solto (chip, menu): "Hoje", "Amanhã", "qui, 19 nov". Dentro de frase use [relativeDay]. */
    fun relativeDayLabel(date: LocalDate, today: LocalDate): String =
        relativeDay(date, today).let { if (date.toEpochDays() - today.toEpochDays() <= 1L) it.replaceFirstChar(Char::uppercaseChar) else it }

    /** "1h 45min" · "28min" · "45s" (segundos so no ultimo minuto) · "2 dias" acima de 48h. */
    fun duration(d: Duration): String {
        val total = d.inWholeSeconds.coerceAtLeast(0)
        if (total < 60) return "${total}s"
        val minutesTotal = total / 60 // piso, igual ao SCCountdownTimer
        val h = minutesTotal / 60
        val m = minutesTotal % 60
        return when {
            h >= 48 -> "${h / 24} dias"
            h == 0L -> "${m}min"
            m == 0L -> "${h}h"
            else -> "${h}h ${m}min"
        }
    }

    /** "4h" / "30min" / "1h30" (duracao curta de regra: hold, antecedencia). */
    fun shortHours(d: Duration): String {
        val minutes = d.inWholeMinutes
        val h = minutes / 60
        val m = minutes % 60
        return when {
            h == 0L -> "${m}min"
            m == 0L -> "${h}h"
            else -> "${h}h${m.toString().padStart(2, '0')}"
        }
    }

    /** Celular mascarado: "+55 11 9••••-4321" */
    fun maskedPhone(phone: String): String {
        val digits = phone.filter { it.isDigit() }
        if (digits.length < 10) return phone
        val local = digits.takeLast(11)
        val ddd = local.take(2)
        return "($ddd) ${local[2]}••••-${local.takeLast(4)}"
    }
}

/** Atalho: `money.brl` = "R$ 21,50". */
val Money.brl: String get() = format()
