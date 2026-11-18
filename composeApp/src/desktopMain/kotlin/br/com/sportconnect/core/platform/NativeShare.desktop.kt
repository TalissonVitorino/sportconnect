package br.com.sportconnect.core.platform

import androidx.compose.runtime.Composable

/** Desktop nao tem folha de compartilhar: quem chama copia o texto. */
@Composable
actual fun rememberNativeShare(): (text: String) -> Boolean = { false }
