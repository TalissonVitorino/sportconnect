package br.com.sportconnect.core.designsystem.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/** Raios (05 §10 / 06 §4.2). */
object SCRadius {
    val none = 0.dp
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 24.dp
    val xxl = 28.dp
}

/** Formas prontas por papel. */
object SCShape {
    val badge = RoundedCornerShape(SCRadius.xs)
    val chip = RoundedCornerShape(SCRadius.sm)
    val control = RoundedCornerShape(SCRadius.md) // botao, campo, slot, snackbar
    val small = RoundedCornerShape(SCRadius.lg) // QR, skeleton de card
    val card = RoundedCornerShape(SCRadius.xl) // SCGameCard, SCCourtCard
    val dialog = RoundedCornerShape(SCRadius.xxl)
    val sheet = RoundedCornerShape(topStart = SCRadius.xxl, topEnd = SCRadius.xxl)
    val segment = RoundedCornerShape(2.dp)
    val full = CircleShape
}

val SCShapes = Shapes(
    extraSmall = RoundedCornerShape(SCRadius.xs),
    small = RoundedCornerShape(SCRadius.sm),
    medium = RoundedCornerShape(SCRadius.md),
    large = RoundedCornerShape(SCRadius.xl),
    extraLarge = RoundedCornerShape(SCRadius.xxl),
)

/** Elevacao tonal (05 §11 / 06 §4.3). Valores em dp para tonal/shadow elevation. */
object SCElevation {
    val level0 = 0.dp
    val level1 = 1.dp
    val level2 = 3.dp
    val level3 = 6.dp
    val level4 = 8.dp
    val level5 = 12.dp
}
