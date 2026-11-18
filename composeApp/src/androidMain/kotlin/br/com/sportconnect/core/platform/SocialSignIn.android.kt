package br.com.sportconnect.core.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import br.com.sportconnect.core.config.FeatureFlags
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch

actual fun availableSocialProviders(): Set<SocialProvider> =
    if (FeatureFlags.GOOGLE_WEB_CLIENT_ID.isNotBlank()) setOf(SocialProvider.GOOGLE) else emptySet()

/** Google via Credential Manager (sem a antiga GoogleSignIn API). Apple no Android ficaria via web: fora do MVP. */
@Composable
actual fun rememberSocialSignIn(onResult: (SocialSignInResult) -> Unit): (SocialProvider) -> Unit {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val callback = rememberUpdatedState(onResult)
    return { provider ->
        if (provider != SocialProvider.GOOGLE || FeatureFlags.GOOGLE_WEB_CLIENT_ID.isBlank()) {
            callback.value(SocialSignInResult.Failed("provedor nao configurado"))
        } else {
            scope.launch {
                val option = GetGoogleIdOption.Builder()
                    .setServerClientId(FeatureFlags.GOOGLE_WEB_CLIENT_ID)
                    .setFilterByAuthorizedAccounts(false)
                    .build()
                val request = GetCredentialRequest.Builder().addCredentialOption(option).build()
                val result = try {
                    val credential = CredentialManager.create(context).getCredential(context, request).credential
                    if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                        val google = GoogleIdTokenCredential.createFrom(credential.data)
                        SocialSignInResult.Success(SocialProvider.GOOGLE, google.idToken, google.id, google.displayName)
                    } else {
                        SocialSignInResult.Failed("credencial inesperada")
                    }
                } catch (_: GetCredentialCancellationException) {
                    SocialSignInResult.Cancelled
                } catch (e: Exception) {
                    SocialSignInResult.Failed(e.message ?: "erro")
                }
                callback.value(result)
            }
        }
    }
}
