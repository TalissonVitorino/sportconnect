package br.com.sportconnect.core.domain

import kotlinx.datetime.LocalDate

/** Bandeiras aceitas pelo gateway (detecao pelo inicio do numero). */
enum class CardBrand(val label: String) { VISA("Visa"), MASTERCARD("Mastercard"), ELO("Elo"), AMEX("Amex"), HIPERCARD("Hipercard"), OUTRA("Cartão") }

/**
 * Validacao local do cartao (E5) — so para dar retorno na hora. Os dados do cartao NUNCA vao para o nosso
 * servidor nem ficam no aparelho: em producao o SDK do gateway tokeniza e so o token trafega (PCI-DSS).
 */
object CardValidation {
    private val eloPrefixes = listOf("401178", "401179", "431274", "438935", "451416", "457393", "457631", "457632", "504175", "506699", "5067", "509", "627780", "636297", "636368", "650", "6516", "6550")

    fun brand(number: String): CardBrand {
        val d = number.filter(Char::isDigit)
        return when {
            eloPrefixes.any { d.startsWith(it) } -> CardBrand.ELO
            d.startsWith("606282") || d.startsWith("3841") -> CardBrand.HIPERCARD
            d.startsWith("34") || d.startsWith("37") -> CardBrand.AMEX
            d.startsWith("4") -> CardBrand.VISA
            d.take(2).toIntOrNull() in 51..55 || d.take(4).toIntOrNull() in 2221..2720 -> CardBrand.MASTERCARD
            else -> CardBrand.OUTRA
        }
    }

    /** Algoritmo de Luhn + tamanho (Amex 15, demais 16; Hipercard ate 19). */
    fun numberValid(number: String): Boolean {
        val d = number.filter(Char::isDigit)
        if (d.length !in 15..19) return false
        if (brand(d) == CardBrand.AMEX && d.length != 15) return false
        var sum = 0
        d.reversed().forEachIndexed { i, ch ->
            var n = ch - '0'
            if (i % 2 == 1) { n *= 2; if (n > 9) n -= 9 }
            sum += n
        }
        return sum % 10 == 0
    }

    /** "MMAA" (so digitos). Valido ate o ultimo dia do mes. */
    fun expiryValid(mmyy: String, today: LocalDate): Boolean = expiryState(mmyy, today) == ExpiryState.Ok

    enum class ExpiryState { Incomplete, Invalid, Expired, Ok }

    fun expiryState(mmyy: String, today: LocalDate): ExpiryState {
        val d = mmyy.filter(Char::isDigit)
        if (d.length < 4) return ExpiryState.Incomplete
        val month = d.take(2).toInt()
        val year = 2000 + d.substring(2, 4).toInt()
        if (month !in 1..12) return ExpiryState.Invalid
        val current = today.year * 12 + (today.month.ordinal + 1)
        return if (year * 12 + month < current) ExpiryState.Expired else ExpiryState.Ok
    }

    fun cvvValid(cvv: String, brand: CardBrand): Boolean = cvv.filter(Char::isDigit).length == if (brand == CardBrand.AMEX) 4 else 3

    /** Cartao de teste que o gateway fake recusa (E4: `error_payment_declined_*`). */
    const val DECLINED_TEST_NUMBER = "4000000000000002"
}
