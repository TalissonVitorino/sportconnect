package br.com.sportconnect.core.designsystem

import androidx.compose.ui.text.AnnotatedString
import br.com.sportconnect.core.designsystem.component.MaskVisualTransformation
import br.com.sportconnect.core.designsystem.component.PHONE_MASK
import kotlin.test.Test
import kotlin.test.assertEquals

/** T1: celular no formato do 07 e cursor no lugar certo. */
class PhoneMaskTest {
    private val mask = MaskVisualTransformation(PHONE_MASK)

    @Test
    fun celular_completo_no_formato_do_07() {
        assertEquals("(11) 98765-4321", mask.filter(AnnotatedString("11987654321")).text.text)
    }

    @Test
    fun digitacao_parcial_e_cursor() {
        val t = mask.filter(AnnotatedString("1198"))
        assertEquals("(11) 98", t.text.text)
        assertEquals(7, t.offsetMapping.originalToTransformed(4))
        assertEquals(4, t.offsetMapping.transformedToOriginal(7))
    }
}
