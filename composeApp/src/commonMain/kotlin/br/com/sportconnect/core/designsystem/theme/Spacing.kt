package br.com.sportconnect.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Espacamento — base 4dp, ritmo 8dp (06 §4.1). */
@Immutable
data class SCSpacing(
    /** 4 — icone <-> texto em badge. */
    val xxs: Dp = 4.dp,
    /** 8 — entre chips; icone <-> texto em botao. */
    val xs: Dp = 8.dp,
    /** 12 — padding vertical de lista densa; gap da grade de horarios. */
    val sm: Dp = 12.dp,
    /** 16 — margem de tela (compact), padding de card. */
    val md: Dp = 16.dp,
    /** 20 — padding de sheet. */
    val mdPlus: Dp = 20.dp,
    /** 24 — margem medium+, padding horizontal de botao, entre secoes. */
    val lg: Dp = 24.dp,
    /** 32 — entre grupos de conteudo. */
    val xl: Dp = 32.dp,
    /** 40 — topo de estado vazio. */
    val xxl: Dp = 40.dp,
    /** 48 — alvo de toque minimo. */
    val touch: Dp = 48.dp,
    /** 64 — respiro de hero. */
    val xxxl: Dp = 64.dp,
    /** 80 — espaco para FAB/botao fixo inferior. */
    val bottomSafe: Dp = 80.dp,
)

val LocalSCSpacing = staticCompositionLocalOf { SCSpacing() }

/** Breakpoints de Window Size Class (05 §9). */
object SCBreakpoints {
    val medium = 600.dp
    val expanded = 840.dp
    val large = 1200.dp
    val maxContentWidth = 1040.dp
}
