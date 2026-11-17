package br.com.sportconnect.core

import br.com.sportconnect.core.domain.CardBrand
import br.com.sportconnect.core.domain.CardValidation
import br.com.sportconnect.core.domain.CardValidation.ExpiryState
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** E5: validacao local do cartao (numeros de teste publicos das bandeiras). */
class CardValidationTest {
    private val today = LocalDate(2026, 11, 17)

    @Test
    fun luhn_aceita_numeros_de_teste_e_recusa_digito_errado() {
        assertTrue(CardValidation.numberValid("4111 1111 1111 1111"))
        assertTrue(CardValidation.numberValid("5555555555554444"))
        assertTrue(CardValidation.numberValid("378282246310005"))
        assertFalse(CardValidation.numberValid("4111 1111 1111 1112"))
        assertFalse(CardValidation.numberValid("4111"))
    }

    @Test
    fun detecta_bandeiras() {
        assertEquals(CardBrand.VISA, CardValidation.brand("4111111111111111"))
        assertEquals(CardBrand.MASTERCARD, CardValidation.brand("5555555555554444"))
        assertEquals(CardBrand.MASTERCARD, CardValidation.brand("2221000000000009"))
        assertEquals(CardBrand.AMEX, CardValidation.brand("378282246310005"))
        assertEquals(CardBrand.ELO, CardValidation.brand("6362970000457013"))
        assertEquals(CardBrand.HIPERCARD, CardValidation.brand("6062825624254001"))
    }

    @Test
    fun validade_considera_o_mes_inteiro() {
        assertEquals(ExpiryState.Ok, CardValidation.expiryState("1126", today), "vale ate o fim de nov/26")
        assertEquals(ExpiryState.Expired, CardValidation.expiryState("1026", today))
        assertEquals(ExpiryState.Invalid, CardValidation.expiryState("1327", today))
        assertEquals(ExpiryState.Incomplete, CardValidation.expiryState("12", today))
    }

    @Test
    fun cvv_tem_4_digitos_no_amex_e_3_nos_outros() {
        assertTrue(CardValidation.cvvValid("123", CardBrand.VISA))
        assertFalse(CardValidation.cvvValid("1234", CardBrand.VISA))
        assertTrue(CardValidation.cvvValid("1234", CardBrand.AMEX))
    }
}
