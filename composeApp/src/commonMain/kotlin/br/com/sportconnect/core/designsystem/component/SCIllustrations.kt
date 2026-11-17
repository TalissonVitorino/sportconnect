package br.com.sportconnect.core.designsystem.component

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import br.com.sportconnect.core.designsystem.theme.SportConnectTheme
import br.com.sportconnect.core.model.Sport

/** Ilustracoes "linha de cal" (05 §7) para estados vazios. */
enum class SCIllustration { Court, NoResults, Error, Offline }

/**
 * Placeholder de foto de arena (05 §8): bloco `sport.container` com as linhas da quadra do esporte
 * em "cal" (onContainer a 55%). Decorativo.
 */
@Composable
fun SCCourtPlaceholder(sport: Sport, modifier: Modifier = Modifier) {
    val sc = SportConnectTheme.extendedColors.sport(sport)
    Canvas(modifier) {
        drawRect(sc.container)
        // microtextura de pontos (dry-fit) a 8%
        val dot = 10.dp.toPx()
        var y = dot / 2
        var row = 0
        while (y < size.height) {
            var x = if (row % 2 == 0) dot / 2 else dot
            while (x < size.width) {
                drawCircle(sc.onContainer.copy(alpha = 0.08f), 1.dp.toPx(), Offset(x, y))
                x += dot
            }
            y += dot; row++
        }
        drawCourtLines(sport, sc.onContainer.copy(alpha = 0.55f), inset = 0.14f)
    }
}

internal fun DrawScope.drawCourtLines(sport: Sport, color: Color, inset: Float) {
    val sw = 2.5.dp.toPx()
    val w = size.width
    val h = size.height
    val left = w * inset
    val top = h * inset
    val cw = w - 2 * left
    val ch = h - 2 * top
    val stroke = Stroke(sw, cap = StrokeCap.Round)
    drawRoundRect(color, Offset(left, top), Size(cw, ch), CornerRadius(2.dp.toPx()), style = stroke)
    val cx = w / 2
    val cy = h / 2
    when (sport) {
        Sport.FUTEBOL_SOCIETY, Sport.FUTSAL, Sport.HANDEBOL -> {
            drawLine(color, Offset(cx, top), Offset(cx, top + ch), sw)
            drawCircle(color, ch * 0.18f, Offset(cx, cy), style = stroke)
            val boxW = cw * 0.12f
            val boxH = ch * 0.44f
            drawRect(color, Offset(left, cy - boxH / 2), Size(boxW, boxH), style = stroke)
            drawRect(color, Offset(left + cw - boxW, cy - boxH / 2), Size(boxW, boxH), style = stroke)
        }
        Sport.BASQUETE -> {
            drawLine(color, Offset(cx, top), Offset(cx, top + ch), sw)
            drawCircle(color, ch * 0.16f, Offset(cx, cy), style = stroke)
            drawArc(color, -90f, 180f, false, Offset(left - ch * 0.35f, cy - ch * 0.35f), Size(ch * 0.7f, ch * 0.7f), style = stroke)
            drawArc(color, 90f, 180f, false, Offset(left + cw - ch * 0.35f, cy - ch * 0.35f), Size(ch * 0.7f, ch * 0.7f), style = stroke)
        }
        Sport.TENIS, Sport.BEACH_TENNIS, Sport.PADEL -> {
            drawLine(color, Offset(cx, top - 4.dp.toPx()), Offset(cx, top + ch + 4.dp.toPx()), sw * 1.4f) // rede
            drawLine(color, Offset(left, top + ch * 0.12f), Offset(left + cw, top + ch * 0.12f), sw)
            drawLine(color, Offset(left, top + ch * 0.88f), Offset(left + cw, top + ch * 0.88f), sw)
            drawLine(color, Offset(left + cw * 0.22f, cy), Offset(left + cw * 0.78f, cy), sw)
            drawLine(color, Offset(left + cw * 0.22f, top + ch * 0.12f), Offset(left + cw * 0.22f, top + ch * 0.88f), sw)
            drawLine(color, Offset(left + cw * 0.78f, top + ch * 0.12f), Offset(left + cw * 0.78f, top + ch * 0.88f), sw)
        }
        Sport.VOLEI, Sport.VOLEI_DE_PRAIA, Sport.FUTEVOLEI -> {
            drawLine(color, Offset(cx, top - 6.dp.toPx()), Offset(cx, top + ch + 6.dp.toPx()), sw * 1.6f) // rede
            drawLine(color, Offset(cx - cw * 0.17f, top), Offset(cx - cw * 0.17f, top + ch), sw)
            drawLine(color, Offset(cx + cw * 0.17f, top), Offset(cx + cw * 0.17f, top + ch), sw)
        }
    }
}

/** Desenho das ilustracoes dos estados vazios, nas cores do tema (mudam no escuro). */
@Composable
fun SCIllustrationImage(illustration: SCIllustration, modifier: Modifier = Modifier) {
    val c = SportConnectTheme.colors
    val plane = when (illustration) {
        SCIllustration.Court, SCIllustration.NoResults -> c.primaryContainer
        SCIllustration.Error -> c.errorContainer
        SCIllustration.Offline -> c.surfaceContainerHighest
    }
    // "cal": linha clara em qualquer tema
    val line = if (c.surface.luminance() < 0.5f) c.onSurface else c.surfaceContainerLowest
    val accent = c.secondary
    val ink = when (illustration) {
        SCIllustration.Court, SCIllustration.NoResults -> c.onPrimaryContainer
        SCIllustration.Error -> c.onErrorContainer
        SCIllustration.Offline -> c.onSurfaceVariant
    }
    Canvas(modifier) {
        val s = size.minDimension
        val origin = Offset((size.width - s) / 2, (size.height - s) / 2)
        val sw = 3.dp.toPx()
        // plano chapado arredondado, levemente inclinado (quadra em perspectiva "flat")
        rotate(-8f, Offset(origin.x + s / 2, origin.y + s / 2)) {
            drawRoundRect(plane, origin + Offset(s * 0.08f, s * 0.2f), Size(s * 0.84f, s * 0.6f), CornerRadius(s * 0.08f))
            val l = origin.x + s * 0.16f
            val t = origin.y + s * 0.28f
            val w = s * 0.68f
            val h = s * 0.44f
            drawRoundRect(line, Offset(l, t), Size(w, h), CornerRadius(4f), style = Stroke(sw))
            drawLine(line, Offset(l + w / 2, t), Offset(l + w / 2, t + h), sw)
            drawCircle(line, h * 0.2f, Offset(l + w / 2, t + h / 2), style = Stroke(sw))
        }
        when (illustration) {
            SCIllustration.Court -> {
                drawCircle(accent, s * 0.07f, origin + Offset(s * 0.74f, s * 0.2f))
                drawArc(ink.copy(alpha = 0.5f), 200f, 80f, false, origin + Offset(s * 0.52f, s * 0.12f), Size(s * 0.3f, s * 0.3f), style = Stroke(2.dp.toPx(), cap = StrokeCap.Round))
            }
            SCIllustration.NoResults -> {
                val center = origin + Offset(s * 0.62f, s * 0.42f)
                drawCircle(c.surfaceContainerLowest, s * 0.16f, center)
                drawCircle(ink, s * 0.16f, center, style = Stroke(sw * 1.4f))
                drawLine(ink, center + Offset(s * 0.12f, s * 0.12f), center + Offset(s * 0.26f, s * 0.26f), sw * 2f, StrokeCap.Round)
            }
            SCIllustration.Error, SCIllustration.Offline -> {
                // rede caida: poste + rede em zigue-zague + bola parada
                val baseY = origin.y + s * 0.84f
                drawLine(ink, Offset(origin.x + s * 0.3f, baseY), Offset(origin.x + s * 0.3f, origin.y + s * 0.46f), sw, StrokeCap.Round)
                var x = origin.x + s * 0.3f
                var up = true
                while (x < origin.x + s * 0.72f) {
                    val nx = x + s * 0.06f
                    drawLine(ink, Offset(x, if (up) origin.y + s * 0.5f else origin.y + s * 0.58f), Offset(nx, if (up) origin.y + s * 0.58f else origin.y + s * 0.66f), sw * 0.7f, StrokeCap.Round)
                    x = nx; up = !up
                }
                drawCircle(accent, s * 0.06f, Offset(origin.x + s * 0.78f, baseY - s * 0.06f))
                if (illustration == SCIllustration.Offline) {
                    drawLine(ink, origin + Offset(s * 0.2f, s * 0.16f), origin + Offset(s * 0.82f, s * 0.86f), sw * 1.2f, StrokeCap.Round)
                }
            }
        }
    }
}
