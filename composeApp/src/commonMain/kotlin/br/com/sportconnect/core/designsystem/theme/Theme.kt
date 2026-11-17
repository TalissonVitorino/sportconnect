package br.com.sportconnect.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Relogio usado pelos componentes com tempo (SCCountdownTimer, SCPixPaymentSheet, SCGameCard).
 * Previews, testes e o catalogo fornecem um relogio fixo para resultados deterministicos.
 */
@OptIn(ExperimentalTime::class)
val LocalSCClock = staticCompositionLocalOf<Clock> { Clock.System }

/** Tema do SportConnect ("Placar DS") — Material 3 + tokens estendidos. */
@Composable
fun SportConnectTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) SCDarkColorScheme else SCLightColorScheme
    val ext = if (darkTheme) SCDarkExtendedColors else SCLightExtendedColors
    val display = SairaCondensed
    val text = AtkinsonHyperlegibleNext
    val mono = AtkinsonHyperlegibleMono
    val typography = remember(display, text) { scTypography(display, text) }
    val typographyExt = remember(display, mono) { scTypographyExt(display, mono) }
    CompositionLocalProvider(
        LocalSCExtendedColors provides ext,
        LocalSCSpacing provides SCSpacing(),
        LocalSCTypographyExt provides typographyExt,
    ) {
        MaterialTheme(
            colorScheme = colors,
            typography = typography,
            shapes = SCShapes,
            content = content,
        )
    }
}

/** Acesso aos tokens do tema atual: `SportConnectTheme.extendedColors.success` etc. */
object SportConnectTheme {
    val colors: ColorScheme
        @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme

    val extendedColors: SCExtendedColors
        @Composable @ReadOnlyComposable get() = LocalSCExtendedColors.current

    val spacing: SCSpacing
        @Composable @ReadOnlyComposable get() = LocalSCSpacing.current

    val typography: Typography
        @Composable @ReadOnlyComposable get() = MaterialTheme.typography

    val typographyExt: SCTypographyExt
        @Composable @ReadOnlyComposable get() = LocalSCTypographyExt.current

    val shapes: Shapes
        @Composable @ReadOnlyComposable get() = MaterialTheme.shapes

    val reducedMotion: Boolean
        @Composable @ReadOnlyComposable get() = LocalSCReducedMotion.current
}
