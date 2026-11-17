package br.com.sportconnect.feature.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp

/**
 * "Linha de cal" (10 §3.1/§3.2): meia quadra em traco 2dp — linha de meio-campo atravessando de
 * ponta a ponta e circulo central cortado pela borda direita. Decorativo (sem semantica).
 */
@Composable
fun CourtLinesBackdrop(color: Color, modifier: Modifier = Modifier, lineYFraction: Float = 0.42f) {
    Canvas(modifier.fillMaxSize().clearAndSetSemantics { }) {
        // C2: o circulo ficava enorme em cabecalhos altos e cruzava titulo/corpo. Agora o raio e limitado
        // pela largura, o centro fica quase na borda (so ~1/4 da largura e ocupado) e o traco e mais suave.
        val sw = 2.dp.toPx()
        val y = size.height * lineYFraction
        val r = minOf(size.height * 0.36f, size.width * 0.30f)
        val cx = size.width + r * 0.15f
        val art = color.copy(alpha = color.alpha * 0.55f)
        drawLine(art, Offset(cx - r, y), Offset(size.width, y), sw)
        drawCircle(art, r, Offset(cx, y), style = Stroke(sw))
    }
}
