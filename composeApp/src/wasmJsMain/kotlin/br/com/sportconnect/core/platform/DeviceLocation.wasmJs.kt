package br.com.sportconnect.core.platform

import androidx.compose.runtime.Composable

actual val deviceLocationSupported: Boolean = false

@Composable
actual fun rememberLocationRequest(onResult: (LocationResult) -> Unit): () -> Unit = { onResult(LocationResult.Unavailable) }
