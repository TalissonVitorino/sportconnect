package br.com.sportconnect.core.platform

import androidx.compose.runtime.Composable

private fun webShareAvailable(): Boolean = js("typeof navigator !== 'undefined' && typeof navigator.share === 'function'")

/** Web Share API (celular e alguns navegadores de desktop). Cancelar a folha nao e erro. */
private fun webShare(text: String): Unit = js("navigator.share({ text: text }).catch(function () {})")

@Composable
actual fun rememberNativeShare(): (text: String) -> Boolean = { text ->
    if (webShareAvailable()) {
        webShare(text)
        true
    } else {
        false
    }
}
