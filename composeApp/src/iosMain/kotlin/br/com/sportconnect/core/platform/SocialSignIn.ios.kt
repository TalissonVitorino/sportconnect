package br.com.sportconnect.core.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import kotlinx.cinterop.BetaInteropApi
import platform.AuthenticationServices.ASAuthorization
import platform.AuthenticationServices.ASAuthorizationAppleIDCredential
import platform.AuthenticationServices.ASAuthorizationAppleIDProvider
import platform.AuthenticationServices.ASAuthorizationController
import platform.AuthenticationServices.ASAuthorizationControllerDelegateProtocol
import platform.AuthenticationServices.ASAuthorizationControllerPresentationContextProvidingProtocol
import platform.AuthenticationServices.ASAuthorizationErrorCanceled
import platform.AuthenticationServices.ASAuthorizationScopeEmail
import platform.AuthenticationServices.ASAuthorizationScopeFullName
import platform.AuthenticationServices.ASPresentationAnchor
import platform.Foundation.NSError
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.UIKit.UIApplication
import platform.UIKit.UIWindow
import platform.darwin.NSObject

/** "Sign in with Apple" (AuthenticationServices). Exige a capability no Xcode; ver docs/12-checklist-publicacao.md. */
actual fun availableSocialProviders(): Set<SocialProvider> = setOf(SocialProvider.APPLE)

@Composable
actual fun rememberSocialSignIn(onResult: (SocialSignInResult) -> Unit): (SocialProvider) -> Unit {
    val callback = rememberUpdatedState(onResult)
    val helper = remember { AppleSignInHelper() }
    return { provider ->
        if (provider == SocialProvider.APPLE) helper.start { callback.value(it) }
        else callback.value(SocialSignInResult.Failed("Google no iOS precisa do SDK GoogleSignIn"))
    }
}

private class AppleSignInHelper :
    NSObject(),
    ASAuthorizationControllerDelegateProtocol,
    ASAuthorizationControllerPresentationContextProvidingProtocol {
    private var pending: ((SocialSignInResult) -> Unit)? = null
    private var controller: ASAuthorizationController? = null

    fun start(onResult: (SocialSignInResult) -> Unit) {
        pending = onResult
        val request = ASAuthorizationAppleIDProvider().createRequest().apply {
            requestedScopes = listOf(ASAuthorizationScopeFullName, ASAuthorizationScopeEmail)
        }
        controller = ASAuthorizationController(listOf(request)).also {
            it.delegate = this
            it.presentationContextProvider = this
            it.performRequests()
        }
    }

    @OptIn(BetaInteropApi::class)
    override fun authorizationController(controller: ASAuthorizationController, didCompleteWithAuthorization: ASAuthorization) {
        val credential = didCompleteWithAuthorization.credential as? ASAuthorizationAppleIDCredential
        val token = credential?.identityToken?.let { NSString.create(data = it, encoding = NSUTF8StringEncoding)?.toString() }
        val name = credential?.fullName?.let { listOfNotNull(it.givenName, it.familyName).joinToString(" ").ifBlank { null } }
        finish(
            if (token != null) SocialSignInResult.Success(SocialProvider.APPLE, token, credential.email, name)
            else SocialSignInResult.Failed("sem token"),
        )
    }

    override fun authorizationController(controller: ASAuthorizationController, didCompleteWithError: NSError) {
        finish(if (didCompleteWithError.code == ASAuthorizationErrorCanceled) SocialSignInResult.Cancelled else SocialSignInResult.Failed(didCompleteWithError.localizedDescription))
    }

    override fun presentationAnchorForAuthorizationController(controller: ASAuthorizationController): ASPresentationAnchor =
        UIApplication.sharedApplication.keyWindow ?: UIWindow()

    private fun finish(result: SocialSignInResult) {
        val cb = pending ?: return
        pending = null
        controller = null
        cb(result)
    }
}
