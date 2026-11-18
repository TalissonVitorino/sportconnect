package br.com.sportconnect.server.auth

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class InMemoryOtpStoreTest {
    @Test
    fun codigo_gerado_valida_uma_unica_vez() {
        val store = InMemoryOtpStore()
        val codigo = store.gerar("+5511988887777")
        assertEquals(OtpResultado.Ok, store.validar("+5511988887777", codigo))
        assertEquals(OtpResultado.Invalido, store.validar("+5511988887777", codigo)) // uso unico
    }

    @Test
    fun bloqueia_exatamente_na_quinta_tentativa_errada() {
        val store = InMemoryOtpStore()
        store.gerar("+5511988887777")
        repeat(4) { assertEquals(OtpResultado.Invalido, store.validar("+5511988887777", "000000")) }
        val quinta = store.validar("+5511988887777", "000000")
        assertIs<OtpResultado.Bloqueado>(quinta)
    }
}
