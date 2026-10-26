package com.kikepb.core.domain.auth.repository

import com.kikepb.core.domain.auth.model.SignInMethod
import com.kikepb.core.domain.auth.model.SocialCredential
import com.kikepb.core.domain.auth.model.SocialCredentialResult

/**
 * The platform's sign-in sheets (spec 019 AC-019-23): Credential Manager on Android (Google), AuthenticationServices
 * and the Google Sign-In SDK on iOS (Apple and Google). The own-backend variant offers none.
 */
interface SocialCredentialProvider {
    /** The methods this platform offers, in the order they are shown (Apple first on iOS, as App Review asks). */
    val availableMethods: List<SignInMethod>
    suspend fun obtainCredential(method: SignInMethod): SocialCredentialResult
    /** Revokes the app's access at the provider when the account is deleted (Apple requires it). Best effort. */
    suspend fun revokeAccess(credential: SocialCredential)
}

/** Builds without third-party sign-in (the frozen own backend). */
object NoSocialCredentialProvider : SocialCredentialProvider {
    override val availableMethods: List<SignInMethod> = emptyList()
    override suspend fun obtainCredential(method: SignInMethod) = SocialCredentialResult.Unavailable
    override suspend fun revokeAccess(credential: SocialCredential) = Unit
}
