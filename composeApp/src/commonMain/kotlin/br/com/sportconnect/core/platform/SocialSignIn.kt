package br.com.sportconnect.core.platform

import androidx.compose.runtime.Composable

enum class SocialProvider(val analytics: String) { GOOGLE("google"), APPLE("apple") }

sealed interface SocialSignInResult {
    /** [idToken]: JWT do provedor, que o back-end valida antes de criar a sessao. */
    data class Success(val provider: SocialProvider, val idToken: String, val email: String?, val name: String?) : SocialSignInResult
    data object Cancelled : SocialSignInResult
    data class Failed(val reason: String) : SocialSignInResult
}

/** Provedores que esta plataforma consegue oferecer agora (Android: Google; iOS: Apple; desktop/web: nenhum). */
expect fun availableSocialProviders(): Set<SocialProvider>

@Composable
expect fun rememberSocialSignIn(onResult: (SocialSignInResult) -> Unit): (SocialProvider) -> Unit
