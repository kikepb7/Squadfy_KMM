package org.kikepb.squadfy.auth

import com.kikepb.core.domain.auth.model.SignInMethod
import com.kikepb.core.domain.auth.model.SocialCredential
import com.kikepb.core.domain.auth.model.SocialCredentialResult
import com.kikepb.core.domain.auth.repository.SocialCredentialProvider
import com.kikepb.core.domain.logger.SquadfyLogger
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Sign in with Apple and Google on iOS, through the Swift bridge. Apple goes first: App Review asks for it to be
 * offered at least as prominently as any other third-party sign-in (guideline 4.8).
 */
internal class IosSocialCredentialProvider(private val logger: SquadfyLogger) : SocialCredentialProvider {

    private val bridge: SocialSignInBridge? get() = IosSocialSignIn.bridge

    override val availableMethods: List<SignInMethod>
        get() = if (bridge == null) emptyList() else listOf(SignInMethod.APPLE, SignInMethod.GOOGLE)

    override suspend fun obtainCredential(method: SignInMethod): SocialCredentialResult {
        val bridge = bridge ?: return SocialCredentialResult.Unavailable
        val outcome = suspendCancellableCoroutine { continuation ->
            val onResult: (SocialSignInOutcome) -> Unit = { if (continuation.isActive) continuation.resume(it) }
            when (method) {
                SignInMethod.APPLE -> bridge.signInWithApple(onResult)
                SignInMethod.GOOGLE -> bridge.signInWithGoogle(onResult)
            }
        }
        return outcome.toResult(method)
    }

    override suspend fun revokeAccess(credential: SocialCredential) {
        val code = (credential as? SocialCredential.Apple)?.authorizationCode ?: return
        val bridge = bridge ?: return
        val error = suspendCancellableCoroutine { continuation ->
            bridge.revokeAppleToken(code) { message -> if (continuation.isActive) continuation.resume(message) }
        }
        // The account is deleted anyway: a failed revocation only leaves Squadfy listed in the user's Apple ID
        if (error != null) logger.warn("Unable to revoke the Sign in with Apple token: $error")
    }

    private fun SocialSignInOutcome.toResult(method: SignInMethod): SocialCredentialResult = when (status) {
        SocialSignInOutcome.Status.SUCCESS -> {
            val token = idToken
            when {
                token == null -> SocialCredentialResult.Failed
                method == SignInMethod.APPLE -> {
                    val nonce = rawNonce ?: return SocialCredentialResult.Failed
                    SocialCredentialResult.Success(SocialCredential.Apple(token, nonce, authorizationCode, givenName, familyName))
                }
                else -> SocialCredentialResult.Success(SocialCredential.Google(token))
            }
        }
        SocialSignInOutcome.Status.CANCELLED -> SocialCredentialResult.Cancelled
        SocialSignInOutcome.Status.UNAVAILABLE -> SocialCredentialResult.Unavailable
        SocialSignInOutcome.Status.FAILED -> SocialCredentialResult.Failed
    }
}
