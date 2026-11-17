package br.com.sportconnect.core.designsystem.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import br.com.sportconnect.core.model.Sport

/**
 * Camada 1 — Primitivos (docs/06-design-system.md §2.1).
 * Nao variam por tema e NAO devem ser usados por features: use [SportConnectTheme.colors]
 * e [SportConnectTheme.extendedColors].
 */
internal object SCPalette {
    // verde-quadra (primaria)
    val VerdeQuadra10 = Color(0xFF002107)
    val VerdeQuadra20 = Color(0xFF003916)
    val VerdeQuadra30 = Color(0xFF005323)
    val VerdeQuadra40 = Color(0xFF006D31)
    val VerdeQuadra50 = Color(0xFF008A40)
    val VerdeQuadra60 = Color(0xFF00A74F)
    val VerdeQuadra70 = Color(0xFF00C55E)
    val VerdeQuadra80 = Color(0xFF00E46E)
    val VerdeQuadra85 = Color(0xFF27F37A)
    val VerdeQuadra90 = Color(0xFF64FF93)
    val VerdeQuadra95 = Color(0xFFC3FFCE)
    val VerdeQuadra98 = Color(0xFFE9FFEC)

    // laranja-saibro (secundaria)
    val LaranjaSaibro10 = Color(0xFF2E1500)
    val LaranjaSaibro20 = Color(0xFF532200)
    val LaranjaSaibro30 = Color(0xFF793100)
    val LaranjaSaibro40 = Color(0xFF9E4200)
    val LaranjaSaibro50 = Color(0xFFC65500)
    val LaranjaSaibro60 = Color(0xFFEE6800)
    val LaranjaSaibro70 = Color(0xFFFF8D48)
    val LaranjaSaibro80 = Color(0xFFFFB68E)
    val LaranjaSaibro90 = Color(0xFFFFDBC9)
    val LaranjaSaibro95 = Color(0xFFFFEDE4)

    // azul-refletor (terciaria)
    val AzulRefletor10 = Color(0xFF001945)
    val AzulRefletor20 = Color(0xFF002D6E)
    val AzulRefletor30 = Color(0xFF00429B)
    val AzulRefletor40 = Color(0xFF0058CA)
    val AzulRefletor80 = Color(0xFFBDC2FF)
    val AzulRefletor90 = Color(0xFFDFE0FF)

    // vermelho-falta (erro)
    val VermelhoFalta10 = Color(0xFF3F0400)
    val VermelhoFalta20 = Color(0xFF68000F)
    val VermelhoFalta30 = Color(0xFF93001A)
    val VermelhoFalta40 = Color(0xFFBF0025)
    val VermelhoFalta80 = Color(0xFFFFB4AB)
    val VermelhoFalta90 = Color(0xFFFFDAD6)

    // verde-placar (sucesso)
    val VerdePlacar10 = Color(0xFF002113)
    val VerdePlacar20 = Color(0xFF003823)
    val VerdePlacar30 = Color(0xFF005235)
    val VerdePlacar40 = Color(0xFF006C47)
    val VerdePlacar80 = Color(0xFF52DEA1)
    val VerdePlacar90 = Color(0xFF72FBBD)
    val VerdePlacarSoft90 = Color(0xFFA9F2CC)

    // amarelo-cartao (aviso)
    val AmareloCartao10 = Color(0xFF261A00)
    val AmareloCartao20 = Color(0xFF412D00)
    val AmareloCartao30 = Color(0xFF5E4100)
    val AmareloCartao40 = Color(0xFF7D5800)
    val AmareloCartao80 = Color(0xFFFFBA28)
    val AmareloCartao90 = Color(0xFFFFDEAE)

    // grafite (neutro)
    val Grafite0 = Color(0xFF000000)
    val Grafite4 = Color(0xFF0A100B)
    val Grafite6 = Color(0xFF101411)
    val Grafite10 = Color(0xFF191C19)
    val Grafite12 = Color(0xFF1D211D)
    val Grafite17 = Color(0xFF272B28)
    val Grafite20 = Color(0xFF2D312E)
    val Grafite22 = Color(0xFF323632)
    val Grafite24 = Color(0xFF363A37)
    val Grafite87 = Color(0xFFD6DBD7)
    val Grafite90 = Color(0xFFDEE4DF)
    val Grafite92 = Color(0xFFE4E9E5)
    val Grafite94 = Color(0xFFEAEFEB)
    val Grafite95 = Color(0xFFEDF2EE)
    val Grafite96 = Color(0xFFEFF5F0)
    val Grafite98 = Color(0xFFF5FBF6)
    val Grafite100 = Color(0xFFFFFFFF)

    // grafite-variante (neutro variante)
    val GrafiteVariante30 = Color(0xFF3F4941)
    val GrafiteVariante50 = Color(0xFF6F7A71)
    val GrafiteVariante60 = Color(0xFF88948A)
    val GrafiteVariante80 = Color(0xFFBDCAC0)
    val GrafiteVariante90 = Color(0xFFD9E6DB)

    val White = Color(0xFFFFFFFF)
}

/** Camada 2 — ColorScheme M3 light (06 §2.2, mapeamento exato). */
val SCLightColorScheme: ColorScheme = lightColorScheme(
    primary = Color(0xFF006D31),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF27F37A),
    onPrimaryContainer = Color(0xFF002107),
    inversePrimary = Color(0xFF00E46E),
    secondary = Color(0xFF9E4200),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFFDBC9),
    onSecondaryContainer = Color(0xFF2E1500),
    tertiary = Color(0xFF0058CA),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFDFE0FF),
    onTertiaryContainer = Color(0xFF001945),
    error = Color(0xFFBF0025),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF3F0400),
    background = Color(0xFFF5FBF6),
    onBackground = Color(0xFF191C19),
    surface = Color(0xFFF5FBF6),
    onSurface = Color(0xFF191C19),
    surfaceVariant = Color(0xFFD9E6DB),
    onSurfaceVariant = Color(0xFF3F4941),
    surfaceTint = Color(0xFF006D31),
    inverseSurface = Color(0xFF2D312E),
    inverseOnSurface = Color(0xFFEDF2EE),
    outline = Color(0xFF6F7A71),
    outlineVariant = Color(0xFFBDCAC0),
    scrim = Color(0xFF000000),
    surfaceBright = Color(0xFFF5FBF6),
    surfaceDim = Color(0xFFD6DBD7),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFEFF5F0),
    surfaceContainer = Color(0xFFEAEFEB),
    surfaceContainerHigh = Color(0xFFE4E9E5),
    surfaceContainerHighest = Color(0xFFDEE4DF),
)

/** Camada 2 — ColorScheme M3 dark (06 §2.2, mapeamento exato). */
val SCDarkColorScheme: ColorScheme = darkColorScheme(
    primary = Color(0xFF00E46E),
    onPrimary = Color(0xFF003916),
    primaryContainer = Color(0xFF005323),
    onPrimaryContainer = Color(0xFF64FF93),
    inversePrimary = Color(0xFF006D31),
    secondary = Color(0xFFFFB68E),
    onSecondary = Color(0xFF532200),
    secondaryContainer = Color(0xFF793100),
    onSecondaryContainer = Color(0xFFFFDBC9),
    tertiary = Color(0xFFBDC2FF),
    onTertiary = Color(0xFF002D6E),
    tertiaryContainer = Color(0xFF00429B),
    onTertiaryContainer = Color(0xFFDFE0FF),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF68000F),
    errorContainer = Color(0xFF93001A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF101411),
    onBackground = Color(0xFFDEE4DF),
    surface = Color(0xFF101411),
    onSurface = Color(0xFFDEE4DF),
    surfaceVariant = Color(0xFF3F4941),
    onSurfaceVariant = Color(0xFFBDCAC0),
    surfaceTint = Color(0xFF00E46E),
    inverseSurface = Color(0xFFDEE4DF),
    inverseOnSurface = Color(0xFF2D312E),
    outline = Color(0xFF88948A),
    outlineVariant = Color(0xFF3F4941),
    scrim = Color(0xFF000000),
    surfaceBright = Color(0xFF363A37),
    surfaceDim = Color(0xFF101411),
    surfaceContainerLowest = Color(0xFF0A100B),
    surfaceContainerLow = Color(0xFF191C19),
    surfaceContainer = Color(0xFF1D211D),
    surfaceContainerHigh = Color(0xFF272B28),
    surfaceContainerHighest = Color(0xFF323632),
)

/** Cores de identificacao de um esporte (06 §2.3). Nunca usar para status. */
@Immutable
data class SCSportColors(
    val container: Color,
    val onContainer: Color,
    val accent: Color,
)

/** Par fundo/texto de um estado (reserva, cota). */
@Immutable
data class SCToneColors(
    val container: Color,
    val content: Color,
)

/** Cores de um slot da grade de horarios (06 §2.3 "Estados de horario"). */
@Immutable
data class SCSlotColors(
    val container: Color,
    val content: Color,
    val border: Color,
)

/**
 * Cores semanticas fora do ColorScheme M3: sucesso, aviso, esportes, estados de reserva e de horario.
 * Acesso: `SportConnectTheme.extendedColors`.
 */
@Immutable
data class SCExtendedColors(
    val success: Color,
    val onSuccess: Color,
    val successContainer: Color,
    val onSuccessContainer: Color,
    val warning: Color,
    val onWarning: Color,
    val warningContainer: Color,
    val onWarningContainer: Color,
    /** Fundo fixo do QR Pix: branco em qualquer tema (leitura por apps de banco). */
    val qrContainer: Color,
    val qrModule: Color,
    /** Tons de sucesso/erro para icones sobre `inverseSurface` (snackbar). */
    val inverseSuccess: Color,
    val inverseError: Color,
    private val sports: Map<Sport, SCSportColors>,
    private val reservations: Map<SCReservationTone, SCToneColors>,
    private val slots: Map<SCSlotState, SCSlotColors>,
) {
    fun sport(sport: Sport): SCSportColors = sports.getValue(sport)
    fun reservation(tone: SCReservationTone): SCToneColors = reservations.getValue(tone)
    fun slot(state: SCSlotState): SCSlotColors = slots.getValue(state)
}

private val LightSports = mapOf(
    Sport.FUTEBOL_SOCIETY to SCSportColors(Color(0xFFC3EEBD), Color(0xFF072100), Color(0xFF006E19)),
    Sport.FUTSAL to SCSportColors(Color(0xFFE9DDFF), Color(0xFF0C0961), Color(0xFF664DAF)),
    Sport.VOLEI to SCSportColors(Color(0xFFD3E4FF), Color(0xFF001D35), Color(0xFF0062A0)),
    Sport.VOLEI_DE_PRAIA to SCSportColors(Color(0xFFFCDFAA), Color(0xFF241A00), Color(0xFF775A00)),
    Sport.FUTEVOLEI to SCSportColors(Color(0xFF92F3F0), Color(0xFF00201F), Color(0xFF006A69)),
    Sport.BEACH_TENNIS to SCSportColors(Color(0xFFFFDADA), Color(0xFF41000B), Color(0xFFB22640)),
    Sport.PADEL to SCSportColors(Color(0xFFBEE9FF), Color(0xFF001F2A), Color(0xFF006684)),
    Sport.TENIS to SCSportColors(Color(0xFFE3E7AC), Color(0xFF1C1D00), Color(0xFF586400)),
    Sport.BASQUETE to SCSportColors(Color(0xFFFFDBC9), Color(0xFF2E1500), Color(0xFF9D4404)),
    Sport.HANDEBOL to SCSportColors(Color(0xFFFFD8EB), Color(0xFF3D0028), Color(0xFFA82879)),
)

private val DarkSports = mapOf(
    Sport.FUTEBOL_SOCIETY to SCSportColors(Color(0xFF005310), Color(0xFF9CF896), Color(0xFF80DB7C)),
    Sport.FUTSAL to SCSportColors(Color(0xFF4B3594), Color(0xFFE9DDFF), Color(0xFFD2BBFF)),
    Sport.VOLEI to SCSportColors(Color(0xFF004A7A), Color(0xFFD3E4FF), Color(0xFFA2C9FF)),
    Sport.VOLEI_DE_PRAIA to SCSportColors(Color(0xFF5A4300), Color(0xFFFFDEA2), Color(0xFFEFC053)),
    Sport.FUTEVOLEI to SCSportColors(Color(0xFF00504F), Color(0xFF00FCF9), Color(0xFF00DDDB)),
    Sport.BEACH_TENNIS to SCSportColors(Color(0xFF91002B), Color(0xFFFFDADA), Color(0xFFFFB3B5)),
    Sport.PADEL to SCSportColors(Color(0xFF004D65), Color(0xFFBEE9FF), Color(0xFF6AD3FF)),
    Sport.TENIS to SCSportColors(Color(0xFF424B00), Color(0xFFDFEB73), Color(0xFFC2CF58)),
    Sport.BASQUETE to SCSportColors(Color(0xFF793100), Color(0xFFFFDBC9), Color(0xFFFFB68E)),
    Sport.HANDEBOL to SCSportColors(Color(0xFF8A005F), Color(0xFFFFD8EB), Color(0xFFFFAED9)),
)

val SCLightExtendedColors = SCExtendedColors(
    success = SCPalette.VerdePlacar40,
    onSuccess = SCPalette.White,
    successContainer = SCPalette.VerdePlacarSoft90,
    onSuccessContainer = SCPalette.VerdePlacar10,
    warning = SCPalette.AmareloCartao40,
    onWarning = SCPalette.White,
    warningContainer = SCPalette.AmareloCartao90,
    onWarningContainer = SCPalette.AmareloCartao10,
    qrContainer = SCPalette.White,
    qrModule = SCPalette.Grafite10,
    inverseSuccess = SCPalette.VerdePlacar80,
    inverseError = SCPalette.VermelhoFalta80,
    sports = LightSports,
    reservations = mapOf(
        SCReservationTone.AwaitingPayment to SCToneColors(Color(0xFFFFDEAE), Color(0xFF261A00)),
        SCReservationTone.Confirmed to SCToneColors(Color(0xFFA9F2CC), Color(0xFF002113)),
        SCReservationTone.Cancelled to SCToneColors(Color(0xFFFFDAD6), Color(0xFF3F0400)),
        SCReservationTone.Expired to SCToneColors(Color(0xFFDEE4DF), Color(0xFF3F4941)),
        SCReservationTone.Completed to SCToneColors(Color(0xFFDFE0FF), Color(0xFF001945)),
    ),
    slots = mapOf(
        SCSlotState.Available to SCSlotColors(Color(0xFFFFFFFF), Color(0xFF191C19), Color(0xFF6F7A71)),
        SCSlotState.Busy to SCSlotColors(Color(0xFFE4E9E5), Color(0xFF3F4941), Color(0xFFE4E9E5)),
        SCSlotState.OnHold to SCSlotColors(Color(0xFFFFDEAE), Color(0xFF261A00), Color(0xFF7D5800)),
        SCSlotState.Selected to SCSlotColors(Color(0xFF006D31), Color(0xFFFFFFFF), Color(0xFF006D31)),
    ),
)

val SCDarkExtendedColors = SCExtendedColors(
    success = SCPalette.VerdePlacar80,
    onSuccess = SCPalette.VerdePlacar20,
    successContainer = SCPalette.VerdePlacar30,
    onSuccessContainer = SCPalette.VerdePlacar90,
    warning = SCPalette.AmareloCartao80,
    onWarning = SCPalette.AmareloCartao20,
    warningContainer = SCPalette.AmareloCartao30,
    onWarningContainer = SCPalette.AmareloCartao90,
    qrContainer = SCPalette.White,
    qrModule = SCPalette.Grafite10,
    inverseSuccess = SCPalette.VerdePlacar40,
    inverseError = SCPalette.VermelhoFalta40,
    sports = DarkSports,
    reservations = mapOf(
        SCReservationTone.AwaitingPayment to SCToneColors(Color(0xFF5E4100), Color(0xFFFFDEAE)),
        SCReservationTone.Confirmed to SCToneColors(Color(0xFF005235), Color(0xFF72FBBD)),
        SCReservationTone.Cancelled to SCToneColors(Color(0xFF93001A), Color(0xFFFFDAD6)),
        SCReservationTone.Expired to SCToneColors(Color(0xFF323632), Color(0xFFBDCAC0)),
        SCReservationTone.Completed to SCToneColors(Color(0xFF00429B), Color(0xFFDFE0FF)),
    ),
    slots = mapOf(
        SCSlotState.Available to SCSlotColors(Color(0xFF0A100B), Color(0xFFDEE4DF), Color(0xFF88948A)),
        SCSlotState.Busy to SCSlotColors(Color(0xFF272B28), Color(0xFFBDCAC0), Color(0xFF272B28)),
        SCSlotState.OnHold to SCSlotColors(Color(0xFF5E4100), Color(0xFFFFDEAE), Color(0xFFFFBA28)),
        SCSlotState.Selected to SCSlotColors(Color(0xFF00E46E), Color(0xFF003916), Color(0xFF00E46E)),
    ),
)

val LocalSCExtendedColors = staticCompositionLocalOf { SCLightExtendedColors }
