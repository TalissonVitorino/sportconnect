package br.com.sportconnect.core.designsystem.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import br.com.sportconnect.core.designsystem.theme.SCMotion
import br.com.sportconnect.core.designsystem.theme.SCRadius
import br.com.sportconnect.core.designsystem.theme.SCShape
import br.com.sportconnect.core.designsystem.theme.SportConnectTheme

/** Forma de um bloco de skeleton (06 §5.18). */
@Immutable
sealed interface SCSkeletonShape {
    data class Line(val widthFraction: Float = 1f, val height: Dp = 14.dp) : SCSkeletonShape
    data class Circle(val size: Dp = 40.dp) : SCSkeletonShape
    data class Box(val height: Dp, val radius: Dp = SCRadius.lg, val widthFraction: Float = 1f) : SCSkeletonShape
}

/**
 * Bloco de carregamento com shimmer de 1.200ms (surfaceContainerHigh -> surfaceContainerHighest).
 * Invisivel para leitores de tela; o container pai deve ter `contentDescription = "Carregando jogos"`.
 * Shimmer desligado com reduzir movimento. Nunca usar para valores de pagamento.
 */
@Composable
fun SCSkeleton(
    shape: SCSkeletonShape,
    modifier: Modifier = Modifier,
    animate: Boolean = !SportConnectTheme.reducedMotion,
) {
    val c = SportConnectTheme.colors
    val base = c.surfaceContainerHigh
    val highlight = c.surfaceContainerHighest
    val progress = if (animate) {
        val inf = rememberInfiniteTransition(label = "shimmer")
        val p by inf.animateFloat(0f, 1f, infiniteRepeatable(tween(SCMotion.ShimmerCycle, easing = LinearEasing), RepeatMode.Restart), label = "p")
        p
    } else {
        -1f
    }
    val (sizeMod, clipShape) = when (shape) {
        is SCSkeletonShape.Line -> Modifier.fillMaxWidth(shape.widthFraction).height(shape.height) to RoundedCornerShape(shape.height / 2)
        is SCSkeletonShape.Circle -> Modifier.size(shape.size) to SCShape.full
        is SCSkeletonShape.Box -> Modifier.fillMaxWidth(shape.widthFraction).height(shape.height) to RoundedCornerShape(shape.radius)
    }
    Box(
        modifier
            .then(sizeMod)
            .clip(clipShape)
            .clearAndSetSemantics { }
            .drawWithCache {
                val w = size.width
                onDrawBehind {
                    drawRect(base)
                    if (progress >= 0f) {
                        val x = -w + progress * 3 * w
                        drawRect(
                            Brush.linearGradient(
                                listOf(base, highlight, base),
                                start = Offset(x - w * 0.4f, 0f),
                                end = Offset(x + w * 0.4f, size.height),
                            ),
                        )
                    }
                }
            },
    )
}

@Composable
private fun SkeletonContainer(description: String, modifier: Modifier, content: @Composable () -> Unit) {
    Box(modifier.clearAndSetSemantics { contentDescription = description }) { content() }
}

/** Preset com a forma do SCGameCard. */
@Composable
fun GameCardSkeleton(modifier: Modifier = Modifier, description: String = "Carregando jogos") {
    val c = SportConnectTheme.colors
    SkeletonContainer(description, modifier) {
        Column(
            Modifier.fillMaxWidth().clip(SCShape.card).background(c.surfaceContainerLowest)
                .border(BorderStroke(1.dp, c.outlineVariant), SCShape.card).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                SCSkeleton(SCSkeletonShape.Box(24.dp, SCRadius.sm, 0.35f))
                SCSkeleton(SCSkeletonShape.Box(24.dp, SCRadius.xs, 0.45f))
            }
            SCSkeleton(SCSkeletonShape.Line(0.7f, 18.dp))
            SCSkeleton(SCSkeletonShape.Line(0.55f))
            SCSkeleton(SCSkeletonShape.Line(0.45f))
            SCSkeleton(SCSkeletonShape.Box(6.dp, 2.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SCSkeleton(SCSkeletonShape.Circle(28.dp))
                SCSkeleton(SCSkeletonShape.Circle(28.dp))
                SCSkeleton(SCSkeletonShape.Line(0.4f, 20.dp))
            }
        }
    }
}

/** Preset com a forma do SCCourtCard (List). */
@Composable
fun CourtCardSkeleton(modifier: Modifier = Modifier, description: String = "Carregando quadras") {
    val c = SportConnectTheme.colors
    SkeletonContainer(description, modifier) {
        Column(
            Modifier.fillMaxWidth().clip(SCShape.card).background(c.surfaceContainerLowest)
                .border(BorderStroke(1.dp, c.outlineVariant), SCShape.card),
        ) {
            SCSkeleton(SCSkeletonShape.Box(160.dp, 0.dp))
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SCSkeleton(SCSkeletonShape.Line(0.6f, 18.dp))
                SCSkeleton(SCSkeletonShape.Line(0.8f))
                SCSkeleton(SCSkeletonShape.Line(0.35f, 20.dp))
            }
        }
    }
}

/** Preset com a forma do SCParticipantRow. */
@Composable
fun ParticipantRowSkeleton(modifier: Modifier = Modifier, description: String = "Carregando participantes") {
    SkeletonContainer(description, modifier) {
        Row(Modifier.fillMaxWidth().height(64.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SCSkeleton(SCSkeletonShape.Circle(40.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SCSkeleton(SCSkeletonShape.Line(0.5f, 16.dp))
                SCSkeleton(SCSkeletonShape.Line(0.3f, 12.dp))
            }
        }
    }
}

/** Preset com a forma da grade de horarios. */
@Composable
fun TimeSlotGridSkeleton(modifier: Modifier = Modifier, columns: Int = 2, rows: Int = 3, description: String = "Carregando horários") {
    SkeletonContainer(description, modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            repeat(rows) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    repeat(columns) { SCSkeleton(SCSkeletonShape.Box(56.dp, SCRadius.md), Modifier.weight(1f)) }
                }
            }
        }
    }
}
