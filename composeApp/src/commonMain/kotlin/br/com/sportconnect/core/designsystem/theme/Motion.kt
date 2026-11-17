package br.com.sportconnect.core.designsystem.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.runtime.staticCompositionLocalOf

/** Tokens de movimento (05 §12). */
object SCMotion {
    const val Short1 = 50
    const val Short2 = 100
    const val Short3 = 150
    const val Short4 = 200
    const val Medium1 = 250
    const val Medium2 = 300
    const val Medium4 = 400
    const val Long2 = 500
    const val ExtraLong1 = 700
    const val ShimmerCycle = 1200

    val Standard: Easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)
    val StandardDecelerate: Easing = CubicBezierEasing(0f, 0f, 0f, 1f)
    val StandardAccelerate: Easing = CubicBezierEasing(0.3f, 0f, 1f, 1f)
    val EmphasizedDecelerate: Easing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)
    val EmphasizedAccelerate: Easing = CubicBezierEasing(0.3f, 0f, 0.8f, 0.15f)

    /** Curva de marca: SOMENTE no "Fechou!" (ultimo segmento do rateio, leve overshoot). */
    val Apito: Easing = CubicBezierEasing(0.34f, 1.4f, 0.64f, 1f)

    /** tween que vira troca instantanea quando o usuario pede para reduzir movimento. */
    fun <T> spec(durationMillis: Int, easing: Easing = Standard, reducedMotion: Boolean = false): FiniteAnimationSpec<T> =
        if (reducedMotion) snap() else tween(durationMillis = durationMillis, easing = easing)
}

/**
 * "Reduzir movimento" do sistema. Compose Multiplatform nao expoe a preferencia em todos os alvos,
 * entao cada plataforma (ou a feature de configuracoes) deve prover o valor. Padrao: false.
 */
val LocalSCReducedMotion = staticCompositionLocalOf { false }
