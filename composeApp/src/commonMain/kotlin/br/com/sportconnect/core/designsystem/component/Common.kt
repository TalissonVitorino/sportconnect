package br.com.sportconnect.core.designsystem.component

import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import br.com.sportconnect.core.designsystem.theme.SportConnectTheme
import br.com.sportconnect.core.model.Money
import kotlin.math.abs

/** Acao generica (rotulo + callback) usada por cards, dialogos, estados vazios e linhas. */
@Immutable
data class SCAction(
    val label: String,
    val onClick: () -> Unit,
    val icon: ImageVector? = null,
    /** Nome acessivel quando o rotulo visivel nao basta (ex.: "Lembrar Ana de pagar"). */
    val accessibilityLabel: String? = null,
    val enabled: Boolean = true,
    val loading: Boolean = false,
)

/**
 * Formata centavos em BRL pt-BR: `R$ 1.240,00` (espaco nao separavel entre "R$" e numero).
 * Fica no DS para nao depender de formatadores do dominio; quando `Money.format()` existir no
 * core/model, as features podem passar o texto pronto.
 */
fun formatBrl(cents: Long, withSymbol: Boolean = true): String {
    val negative = cents < 0
    val abs = abs(cents)
    val reais = (abs / 100).toString()
    val centavos = (abs % 100).toString().padStart(2, '0')
    val grouped = buildString {
        reais.forEachIndexed { i, c ->
            if (i > 0 && (reais.length - i) % 3 == 0) append('.')
            append(c)
        }
    }
    val number = "$grouped,$centavos"
    val withSign = if (negative) "-$number" else number
    return if (withSymbol) "R$ $withSign" else withSign
}

fun Money.formatBrl(withSymbol: Boolean = true): String = formatBrl(cents, withSymbol)

/** Leitura natural de valores para leitores de tela: "21 reais e 50 centavos". */
fun spokenBrl(cents: Long): String {
    val r = abs(cents) / 100
    val c = abs(cents) % 100
    val reais = when (r) {
        0L -> null
        1L -> "1 real"
        else -> "$r reais"
    }
    val cent = when (c) {
        0L -> null
        1L -> "1 centavo"
        else -> "$c centavos"
    }
    return listOfNotNull(reais, cent).joinToString(" e ").ifEmpty { "0 reais" }
}

/** Iniciais para avatar ("Ana Souza" -> "AS"). */
internal fun initialsOf(name: String): String =
    name.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
        .let { parts -> (parts.firstOrNull()?.take(1).orEmpty() + if (parts.size > 1) parts.last().take(1) else "") }
        .uppercase()

/**
 * Anel de foco de teclado (06 §4.4): 2dp `primary` com 2dp de afastamento,
 * raio = raio do componente + 2dp. Desenhado fora dos limites, sem alterar layout.
 */
fun Modifier.scFocusRing(
    interactionSource: InteractionSource,
    shape: Shape,
    width: Dp = 2.dp,
    offset: Dp = 2.dp,
): Modifier = composed {
    val focused by interactionSource.collectIsFocusedAsState()
    val color = SportConnectTheme.colors.primary
    if (!focused) {
        this
    } else {
        drawWithContent {
            drawContent()
            val o = (offset + width / 2).toPx()
            val outline = shape.createOutline(
                Size(size.width + 2 * o, size.height + 2 * o),
                layoutDirection,
                this,
            )
            translate(-o, -o) { drawOutline(outline, color, style = Stroke(width.toPx())) }
        }
    }
}

/** Hachura a 45 graus (slot ocupado, cotas pendentes expiradas) — sinal que nao depende de cor. */
fun Modifier.scHatch(color: Color, spacing: Dp = 8.dp, strokeWidth: Dp = 1.dp): Modifier = drawBehind {
    val step = spacing.toPx()
    val sw = strokeWidth.toPx()
    clipRect {
        var x = -size.height
        while (x < size.width) {
            drawLine(color, Offset(x, size.height), Offset(x + size.height, 0f), sw)
            x += step
        }
    }
}
