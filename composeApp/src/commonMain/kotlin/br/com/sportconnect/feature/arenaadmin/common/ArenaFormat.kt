package br.com.sportconnect.feature.arenaadmin.common

import br.com.sportconnect.core.model.FusoSaoPaulo
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

/**
 * Formatacao pt-BR usada no modo arena (07 §datas: "qui, 21 nov", "20h", "19h–20h", "22/11/2026").
 * Funcoes puras (sem locale de plataforma) para rodar igual em JVM, Wasm e iOS.
 */
object ArenaFormat {
    private val weekdays = mapOf(
        DayOfWeek.MONDAY to "seg", DayOfWeek.TUESDAY to "ter", DayOfWeek.WEDNESDAY to "qua",
        DayOfWeek.THURSDAY to "qui", DayOfWeek.FRIDAY to "sex", DayOfWeek.SATURDAY to "sáb", DayOfWeek.SUNDAY to "dom",
    )
    private val months = listOf("jan", "fev", "mar", "abr", "mai", "jun", "jul", "ago", "set", "out", "nov", "dez")

    fun weekdayShort(day: DayOfWeek): String = weekdays.getValue(day)

    /** "qui, 21 nov" */
    fun dayLabel(date: LocalDate): String = "${weekdayShort(date.dayOfWeek)}, ${date.day} ${months[date.month.number - 1]}"

    /** "21 nov" */
    fun dayMonth(date: LocalDate): String = "${date.day} ${months[date.month.number - 1]}"

    /** "21/11" */
    fun shortDate(date: LocalDate): String = "${date.day.pad2()}/${date.month.number.pad2()}"

    /** "21/11/2026" */
    fun fullDate(date: LocalDate): String = "${shortDate(date)}/${date.year}"

    /** ISO "2026-11-21" (CSV). */
    fun isoDate(date: LocalDate): String = date.toString()

    /** "20h" ou "20h30". */
    fun hour(h: Int, m: Int = 0): String = if (m == 0) "${h}h" else "${h}h${m.pad2()}"

    fun hour(instant: Instant): String = instant.local().let { hour(it.hour, it.minute) }

    /** "19h–20h" */
    fun range(start: Instant, end: Instant): String = "${hour(start)}–${hour(end)}"

    fun Instant.local(): LocalDateTime = toLocalDateTime(FusoSaoPaulo)

    fun Instant.localDate(): LocalDate = local().date

    /** "Bruno Lima" -> "Bruno L." */
    fun shortName(full: String?): String? {
        if (full.isNullOrBlank()) return null
        val parts = full.trim().split(' ').filter { it.isNotBlank() }
        return if (parts.size < 2) parts.first() else "${parts.first()} ${parts.last().first()}."
    }

    private fun Int.pad2() = toString().padStart(2, '0')
}
