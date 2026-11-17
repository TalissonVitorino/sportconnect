package br.com.sportconnect.core.designsystem.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import br.com.sportconnect.core.designsystem.theme.SportConnectTheme
import br.com.sportconnect.core.model.Money

enum class SCPriceEmphasis { Normal, Muted, Strong }
enum class SCPriceBreakdownVariant { Payer, Organizer, Arena }

/**
 * Linha do resumo de valores. [valueText] substitui o valor formatado (ex.: "Dividido por 12"
 * sem valor, ou "- R$ 10,00"). [onHintClick] mostra o icone `info` (explicacao acessivel).
 */
@Immutable
data class PriceLine(
    val label: String,
    val value: Money? = null,
    val valueText: String? = null,
    val hint: String? = null,
    val onHintClick: (() -> Unit)? = null,
    val emphasis: SCPriceEmphasis = SCPriceEmphasis.Normal,
)

/**
 * Resumo de valores (06 §5.10): linhas chave-valor tabulares alinhadas a direita, divisor
 * "linha de quadra" de 2dp (com o circulo central) e o total em destaque (heading()).
 * Centavos sempre visiveis.
 */
@Composable
fun SCPriceBreakdown(
    lines: List<PriceLine>,
    total: PriceLine,
    modifier: Modifier = Modifier,
    variant: SCPriceBreakdownVariant = SCPriceBreakdownVariant.Payer,
    footnote: String? = null,
) {
    val c = SportConnectTheme.colors
    val t = MaterialTheme.typography
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        lines.forEach { line ->
            val valueStr = line.valueText ?: line.value?.formatBrl()
            val spoken = line.valueText ?: line.value?.let { spokenBrl(it.cents) }
            Row(
                Modifier.fillMaxWidth().heightIn(min = 32.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    Modifier.weight(1f).clearAndSetSemantics {
                        contentDescription = if (spoken != null) "${line.label}: $spoken" else line.label
                    },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        line.label,
                        style = t.bodyMedium.copy(fontWeight = if (line.emphasis == SCPriceEmphasis.Strong) FontWeight.Bold else FontWeight.Normal),
                        color = if (line.emphasis == SCPriceEmphasis.Muted) c.onSurfaceVariant else c.onSurface,
                    )
                }
                if (line.onHintClick != null) {
                    IconButton(onClick = line.onHintClick, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Rounded.Info, contentDescription = line.hint ?: line.label, tint = c.onSurfaceVariant, modifier = Modifier.size(18.dp))
                    }
                }
                if (valueStr != null) {
                    Text(
                        valueStr,
                        style = t.bodyMedium.copy(
                            fontFeatureSettings = "tnum",
                            fontWeight = if (line.emphasis == SCPriceEmphasis.Strong) FontWeight.Bold else FontWeight.Normal,
                        ),
                        color = if (line.emphasis == SCPriceEmphasis.Muted) c.onSurfaceVariant else c.onSurface,
                        textAlign = TextAlign.End,
                        modifier = Modifier.padding(start = 12.dp).clearAndSetSemantics { },
                    )
                }
            }
        }
        CourtLineDivider(Modifier.padding(vertical = 8.dp))
        val totalStr = total.valueText ?: total.value?.formatBrl().orEmpty()
        val totalSpoken = total.valueText ?: total.value?.let { spokenBrl(it.cents) }.orEmpty()
        Row(
            Modifier.fillMaxWidth().clearAndSetSemantics {
                heading()
                contentDescription = "${total.label}: $totalSpoken"
            },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(total.label, style = t.titleMedium, color = c.onSurface, modifier = Modifier.weight(1f))
            Text(
                totalStr,
                style = SportConnectTheme.typographyExt.numericPrice,
                color = if (variant == SCPriceBreakdownVariant.Arena) c.primary else c.onSurface,
            )
        }
        if (footnote != null) {
            Text(footnote, style = t.bodySmall, color = c.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp))
        }
    }
}

/** Divisor de 2dp com o circulo central da quadra — o "meio-campo" do resumo. */
@Composable
fun CourtLineDivider(modifier: Modifier = Modifier) {
    val color = SportConnectTheme.colors.outlineVariant
    Canvas(modifier.fillMaxWidth().height(12.dp)) {
        val y = size.height / 2
        val sw = 2.dp.toPx()
        val r = 5.dp.toPx()
        val cx = size.width / 2
        drawLine(color, Offset(0f, y), Offset(cx - r, y), sw)
        drawLine(color, Offset(cx + r, y), Offset(size.width, y), sw)
        drawCircle(color, r, Offset(cx, y), style = Stroke(sw))
    }
}
