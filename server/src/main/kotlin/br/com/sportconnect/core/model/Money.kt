package br.com.sportconnect.core.model

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

/**
 * Valor monetario em centavos de real (BRL), evitando ponto flutuante (RN-01).
 * Exibicao no formato `R$ 1.234,56` via [format].
 */
@Serializable
@JvmInline
value class Money(val cents: Long) : Comparable<Money> {
    operator fun plus(other: Money): Money = Money(cents + other.cents)
    operator fun minus(other: Money): Money = Money(cents - other.cents)
    operator fun times(factor: Int): Money = Money(cents * factor)
    operator fun unaryMinus(): Money = Money(-cents)
    override fun compareTo(other: Money): Int = cents.compareTo(other.cents)

    val isZero: Boolean get() = cents == 0L

    /** Formata em pt-BR: `R$ 1.234,56` (negativos: `-R$ 10,00`). */
    fun format(): String {
        val abs = if (cents < 0) -cents else cents
        val reais = abs / 100
        val centavos = (abs % 100).toString().padStart(2, '0')
        val reaisStr = reais.toString().reversed().chunked(3).joinToString(".").reversed()
        // Espaco nao separavel (U+00A0): "R$" nunca fica sozinho no fim da linha (C12).
        return (if (cents < 0) "-" else "") + "R$\u00A0$reaisStr,$centavos"
    }

    override fun toString(): String = format()

    companion object {
        val ZERO = Money(0)
        fun reais(value: Long): Money = Money(value * 100)
    }
}

fun Iterable<Money>.sum(): Money = Money(sumOf { it.cents })
