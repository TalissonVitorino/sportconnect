package br.com.sportconnect.core.platform

import androidx.compose.runtime.Composable

actual fun availableSocialProviders(): Set<SocialProvider> = emptySet()

@Composable
actual fun rememberSocialSignIn(onResult: (SocialSignInResult) -> Unit): (SocialProvider) -> Unit =
    { onResult(SocialSignInResult.Failed("indisponivel nesta plataforma")) }
