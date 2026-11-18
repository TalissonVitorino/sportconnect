package br.com.sportconnect.core.platform

import androidx.compose.runtime.Composable

/**
 * X1 (doc 13): folha nativa de compartilhar (09 §6.5). Android: `ACTION_SEND`; iOS:
 * `UIActivityViewController`; web: `navigator.share`. Devolve false quando a plataforma nao tem folha
 * nativa (desktop, navegadores sem Web Share): quem chama copia o texto e avisa.
 */
@Composable
expect fun rememberNativeShare(): (text: String) -> Boolean
