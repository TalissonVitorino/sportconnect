package br.com.sportconnect.core.designsystem.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import br.com.sportconnect.core.designsystem.theme.SCMotion
import br.com.sportconnect.core.designsystem.theme.SCShareState
import br.com.sportconnect.core.designsystem.theme.SportConnectTheme
import br.com.sportconnect.core.model.Money
import br.com.sportconnect.resources.Res
import br.com.sportconnect.resources.ds_split_count
import br.com.sportconnect.resources.ds_split_legend_covered
import br.com.sportconnect.resources.ds_split_legend_paid
import br.com.sportconnect.resources.ds_split_legend_pending
import br.com.sportconnect.resources.ds_split_legend_processing
import br.com.sportconnect.resources.split_progress
import org.jetbrains.compose.resources.stringResource

enum class SCSplitProgressVariant { Full, Compact, Continuous }

/**
 * O componente-assinatura (06 §5.8): o rateio como SEGMENTOS — uma cota por segmento, como as
 * cadeiras de um banco de reservas. Ordem: pagas · em analise · cobertas · pendentes · reembolsadas.
 *
 * - Processando: primary 40% + listras animadas (estaticas com reduzir movimento) — padrao, nao so cor.
 * - Coberta pelo organizador: tertiary (nunca conta como paga pelo atleta).
 * - Pendente: surfaceContainerHighest + borda 1dp outline. Reembolsada: borda tracejada.
 * - Completa: coreografia "Fechou!" (pulso primary -> primaryContainer com a curva Apito).
 * - [expired]: pendentes com hachura e 38%.
 * - Com N > 30 cotas vira automaticamente `Continuous` (barra continua com marcadores a cada 10).
 */
@Composable
fun SCSplitProgress(
    totalShares: Int,
    paid: Int,
    modifier: Modifier = Modifier,
    processing: Int = 0,
    covered: Int = 0,
    refunded: Int = 0,
    collected: Money? = null,
    total: Money? = null,
    variant: SCSplitProgressVariant = SCSplitProgressVariant.Full,
    showLegend: Boolean = variant == SCSplitProgressVariant.Full,
    expired: Boolean = false,
    onCompleteAnimationEnd: (() -> Unit)? = null,
) {
    val c = SportConnectTheme.colors
    val t = MaterialTheme.typography
    val reduced = SportConnectTheme.reducedMotion
    val n = totalShares.coerceAtLeast(1)
    val effectiveVariant = if (n > 30 && variant != SCSplitProgressVariant.Compact) SCSplitProgressVariant.Continuous else variant
    val compact = variant == SCSplitProgressVariant.Compact
    val complete = paid + covered >= totalShares && totalShares > 0

    // Coreografia "Fechou!": pulso unico quando completa.
    val pulse = remember { Animatable(0f) }
    var wasComplete by remember { mutableStateOf(complete) }
    LaunchedEffect(complete) {
        if (complete && !wasComplete) {
            if (!reduced) {
                pulse.animateTo(1f, tween(SCMotion.Medium2, easing = SCMotion.Apito))
                pulse.animateTo(0f, tween(SCMotion.Medium2, easing = SCMotion.Standard))
            }
            onCompleteAnimationEnd?.invoke()
        }
        wasComplete = complete
    }

    val stripePhase = if (processing > 0 && !reduced) {
        val inf = rememberInfiniteTransition(label = "split-stripes")
        inf.animateFloat(0f, 1f, infiniteRepeatable(tween(SCMotion.ShimmerCycle, easing = LinearEasing), RepeatMode.Restart), label = "phase").value
    } else {
        0f
    }

    val a11y = buildString {
        append(stringResource(Res.string.split_progress, paid, totalShares))
        if (covered > 0) append(", $covered cobertas pelo organizador")
        if (processing > 0) append(", $processing em análise")
        if (collected != null && total != null) {
            append(", ${spokenBrl(collected.cents)} de ${spokenBrl(total.cents)} arrecadados")
        }
    }

    val paidColor = lerp(c.primary, c.primaryContainer, pulse.value)
    val segColors = SegmentColors(
        paid = paidColor,
        processing = c.primary.copy(alpha = 0.4f),
        stripe = c.primary,
        covered = c.tertiary,
        pending = c.surfaceContainerHighest,
        outline = c.outline,
    )

    Column(
        modifier = modifier.clearAndSetSemantics {
            contentDescription = a11y
            progressBarRangeInfo = ProgressBarRangeInfo(paid.toFloat(), 0f..n.toFloat())
        },
        verticalArrangement = Arrangement.spacedBy(if (compact) 6.dp else 8.dp),
    ) {
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (compact) {
                Text(stringResource(Res.string.ds_split_count, paid, totalShares), style = t.titleSmall, color = c.onSurface)
            } else {
                Text("$paid", style = SportConnectTheme.typographyExt.numericScore, color = c.onSurface, modifier = Modifier.alignByBaseline())
                Text(
                    stringResource(Res.string.ds_split_count, paid, totalShares).removePrefix("$paid "),
                    style = t.titleMedium,
                    color = c.onSurfaceVariant,
                    modifier = Modifier.alignByBaseline(),
                )
            }
            if (collected != null && total != null) {
                Box(Modifier.weight(1f), contentAlignment = Alignment.BottomEnd) {
                    Text(
                        "${collected.formatBrl()} de ${total.formatBrl()}",
                        style = if (compact) t.bodySmall else t.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = c.onSurfaceVariant,
                    )
                }
            }
        }
        val barHeight = if (compact) 6.dp else 10.dp
        Canvas(Modifier.fillMaxWidth().height(barHeight)) {
            if (effectiveVariant == SCSplitProgressVariant.Continuous) {
                drawContinuous(n, paid, processing, covered, segColors)
            } else {
                drawSegments(n, paid, processing, covered, refunded, segColors, stripePhase, expired)
            }
        }
        if (showLegend && !compact) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                LegendItem(SCShareState.Paid, stringResource(Res.string.ds_split_legend_paid), paid, segColors)
                if (processing > 0) LegendItem(SCShareState.Processing, stringResource(Res.string.ds_split_legend_processing), processing, segColors)
                if (covered > 0) LegendItem(SCShareState.Covered, stringResource(Res.string.ds_split_legend_covered), covered, segColors)
                val pending = (totalShares - paid - processing - covered - refunded).coerceAtLeast(0)
                LegendItem(SCShareState.Pending, stringResource(Res.string.ds_split_legend_pending), pending, segColors)
            }
        }
    }
}

private data class SegmentColors(
    val paid: Color,
    val processing: Color,
    val stripe: Color,
    val covered: Color,
    val pending: Color,
    val outline: Color,
)

@Composable
private fun LegendItem(state: SCShareState, label: String, count: Int, colors: SegmentColors) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Canvas(Modifier.size(width = 16.dp, height = 10.dp)) {
            drawSegment(Offset.Zero, size, state, colors, 0f, false)
        }
        Text("$label $count", style = MaterialTheme.typography.labelMedium, color = SportConnectTheme.colors.onSurfaceVariant)
    }
}

private fun DrawScope.drawSegment(
    topLeft: Offset,
    size: Size,
    state: SCShareState,
    colors: SegmentColors,
    stripePhase: Float,
    expired: Boolean,
) {
    val r = CornerRadius(2.dp.toPx())
    when (state) {
        SCShareState.Paid -> drawRoundRect(colors.paid, topLeft, size, r)
        SCShareState.Covered -> drawRoundRect(colors.covered, topLeft, size, r)
        SCShareState.Processing -> {
            drawRoundRect(colors.processing, topLeft, size, r)
            val step = 6.dp.toPx()
            val sw = 2.dp.toPx()
            clipRect(topLeft.x, topLeft.y, topLeft.x + size.width, topLeft.y + size.height) {
                var x = topLeft.x - size.height - step + stripePhase * step
                while (x < topLeft.x + size.width) {
                    drawLine(colors.stripe, Offset(x, topLeft.y + size.height), Offset(x + size.height, topLeft.y), sw)
                    x += step
                }
            }
        }
        SCShareState.Pending -> {
            val alpha = if (expired) 0.38f else 1f
            drawRoundRect(colors.pending.copy(alpha = alpha), topLeft, size, r)
            if (expired) {
                clipRect(topLeft.x, topLeft.y, topLeft.x + size.width, topLeft.y + size.height) {
                    var x = topLeft.x - size.height
                    while (x < topLeft.x + size.width) {
                        drawLine(colors.outline.copy(alpha = 0.6f), Offset(x, topLeft.y + size.height), Offset(x + size.height, topLeft.y), 1.dp.toPx())
                        x += 4.dp.toPx()
                    }
                }
            }
            val sw = 1.dp.toPx()
            drawRoundRect(
                colors.outline.copy(alpha = alpha),
                topLeft + Offset(sw / 2, sw / 2),
                Size(size.width - sw, size.height - sw),
                r,
                style = Stroke(sw),
            )
        }
        SCShareState.Refunded -> {
            val sw = 1.dp.toPx()
            drawRoundRect(
                colors.outline,
                topLeft + Offset(sw / 2, sw / 2),
                Size(size.width - sw, size.height - sw),
                r,
                style = Stroke(sw, pathEffect = PathEffect.dashPathEffect(floatArrayOf(3.dp.toPx(), 2.dp.toPx()))),
            )
        }
    }
}

private fun DrawScope.drawSegments(
    n: Int,
    paid: Int,
    processing: Int,
    covered: Int,
    refunded: Int,
    colors: SegmentColors,
    stripePhase: Float,
    expired: Boolean,
) {
    val gap = 3.dp.toPx()
    val w = (size.width - gap * (n - 1)) / n
    for (i in 0 until n) {
        val state = when {
            i < paid -> SCShareState.Paid
            i < paid + processing -> SCShareState.Processing
            i < paid + processing + covered -> SCShareState.Covered
            i >= n - refunded -> SCShareState.Refunded
            else -> SCShareState.Pending
        }
        drawSegment(Offset(i * (w + gap), 0f), Size(w, size.height), state, colors, stripePhase, expired)
    }
}

private fun DrawScope.drawContinuous(n: Int, paid: Int, processing: Int, covered: Int, colors: SegmentColors) {
    val r = CornerRadius(size.height / 2)
    drawRoundRect(colors.pending, size = size, cornerRadius = r)
    val unit = size.width / n
    val track = Path().apply { addRoundRect(RoundRect(0f, 0f, size.width, size.height, r)) }
    clipPath(track) {
        drawRect(colors.paid, size = Size(unit * paid, size.height))
        drawRect(colors.processing, Offset(unit * paid, 0f), Size(unit * processing, size.height))
        drawRect(colors.covered, Offset(unit * (paid + processing), 0f), Size(unit * covered, size.height))
    }
    var k = 10
    while (k < n) {
        val x = unit * k
        drawLine(colors.outline, Offset(x, 0f), Offset(x, size.height), 1.dp.toPx())
        k += 10
    }
}
