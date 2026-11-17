package br.com.sportconnect.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import br.com.sportconnect.resources.Res
import br.com.sportconnect.resources.atkinson_mono_medium
import br.com.sportconnect.resources.atkinson_next_bold
import br.com.sportconnect.resources.atkinson_next_regular
import br.com.sportconnect.resources.atkinson_next_semibold
import br.com.sportconnect.resources.saira_condensed_bold
import br.com.sportconnect.resources.saira_condensed_extrabold
import br.com.sportconnect.resources.saira_condensed_medium
import br.com.sportconnect.resources.saira_condensed_semibold
import org.jetbrains.compose.resources.Font

/**
 * Familias tipograficas (docs/05-visual-design.md §5). TTF estaticos em composeResources/font
 * (Saira Condensed do google/fonts; Atkinson Hyperlegible Next/Mono instanciadas a partir das
 * fontes variaveis do google/fonts com fontTools — sem eixos variaveis, paridade entre alvos).
 */
val SairaCondensed: FontFamily
    @Composable get() = FontFamily(
        Font(Res.font.saira_condensed_medium, FontWeight.Medium),
        Font(Res.font.saira_condensed_semibold, FontWeight.SemiBold),
        Font(Res.font.saira_condensed_bold, FontWeight.Bold),
        Font(Res.font.saira_condensed_extrabold, FontWeight.ExtraBold),
    )

val AtkinsonHyperlegibleNext: FontFamily
    @Composable get() = FontFamily(
        Font(Res.font.atkinson_next_regular, FontWeight.Normal),
        Font(Res.font.atkinson_next_semibold, FontWeight.SemiBold),
        Font(Res.font.atkinson_next_bold, FontWeight.Bold),
    )

val AtkinsonHyperlegibleMono: FontFamily
    @Composable get() = FontFamily(
        Font(Res.font.atkinson_mono_medium, FontWeight.Medium),
    )

/** Escala M3 completa (05 §5.2). */
fun scTypography(display: FontFamily, text: FontFamily): Typography = Typography(
    displayLarge = TextStyle(fontFamily = display, fontWeight = FontWeight.Bold, fontSize = 60.sp, lineHeight = 64.sp, letterSpacing = (-0.5).sp),
    displayMedium = TextStyle(fontFamily = display, fontWeight = FontWeight.Bold, fontSize = 48.sp, lineHeight = 52.sp, letterSpacing = (-0.25).sp),
    displaySmall = TextStyle(fontFamily = display, fontWeight = FontWeight.Bold, fontSize = 38.sp, lineHeight = 44.sp, letterSpacing = 0.sp),
    headlineLarge = TextStyle(fontFamily = display, fontWeight = FontWeight.SemiBold, fontSize = 34.sp, lineHeight = 40.sp, letterSpacing = 0.sp),
    headlineMedium = TextStyle(fontFamily = display, fontWeight = FontWeight.SemiBold, fontSize = 30.sp, lineHeight = 36.sp, letterSpacing = 0.sp),
    headlineSmall = TextStyle(fontFamily = display, fontWeight = FontWeight.SemiBold, fontSize = 26.sp, lineHeight = 32.sp, letterSpacing = 0.sp),
    titleLarge = TextStyle(fontFamily = text, fontWeight = FontWeight.Bold, fontSize = 22.sp, lineHeight = 28.sp, letterSpacing = 0.sp),
    titleMedium = TextStyle(fontFamily = text, fontWeight = FontWeight.Bold, fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.15.sp),
    titleSmall = TextStyle(fontFamily = text, fontWeight = FontWeight.Bold, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp),
    bodyLarge = TextStyle(fontFamily = text, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.15.sp),
    bodyMedium = TextStyle(fontFamily = text, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.25.sp),
    bodySmall = TextStyle(fontFamily = text, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.4.sp),
    labelLarge = TextStyle(fontFamily = text, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp),
    labelMedium = TextStyle(fontFamily = text, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp),
    labelSmall = TextStyle(fontFamily = text, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp),
)

val SCTypography: Typography
    @Composable get() = scTypography(SairaCondensed, AtkinsonHyperlegibleNext)

/** Estilos estendidos do SportConnect (05 §5.2): numeros tabulares e codigo Pix. */
@Immutable
data class SCTypographyExt(
    val numericScore: TextStyle,
    val numericPrice: TextStyle,
    val numericPriceSmall: TextStyle,
    val pixCode: TextStyle,
)

fun scTypographyExt(display: FontFamily, mono: FontFamily): SCTypographyExt = SCTypographyExt(
    numericScore = TextStyle(fontFamily = display, fontWeight = FontWeight.Bold, fontSize = 44.sp, lineHeight = 48.sp, fontFeatureSettings = "tnum"),
    numericPrice = TextStyle(fontFamily = display, fontWeight = FontWeight.Bold, fontSize = 28.sp, lineHeight = 32.sp, fontFeatureSettings = "tnum"),
    // Variante menor usada em cards (06 §5.5/5.6: "numericPrice menor, 20sp").
    numericPriceSmall = TextStyle(fontFamily = display, fontWeight = FontWeight.Bold, fontSize = 20.sp, lineHeight = 24.sp, fontFeatureSettings = "tnum"),
    pixCode = TextStyle(fontFamily = mono, fontWeight = FontWeight.Medium, fontSize = 13.sp, lineHeight = 20.sp),
)

val LocalSCTypographyExt = staticCompositionLocalOf {
    scTypographyExt(FontFamily.Default, FontFamily.Monospace)
}
