package com.kikepb.core.domain.auth.model

/** Third-party sign-in (spec 019 AC-019-23). Email and password stay as the default way in. */
enum class SignInMethod { GOOGLE, APPLE }

/** What the provider's own sign-in sheet returns; the data layer turns it into a Firebase credential. */
sealed interface SocialCredential {
    val method: SignInMethod

    data class Google(val idToken: String) : SocialCredential {
        override val method: SignInMethod get() = SignInMethod.GOOGLE
    }

    /**
     * Apple signs the token with the hash of [rawNonce], which proves it was requested by this sign-in. The name only
     * comes the first time the user authorises the app; [authorizationCode] lets the app revoke the access later.
     */
    data class Apple(
        val idToken: String,
        val rawNonce: String,
        val authorizationCode: String? = null,
        val givenName: String? = null,
        val familyName: String? = null
    ) : SocialCredential {
        override val method: SignInMethod get() = SignInMethod.APPLE
    }
}

sealed interface SocialCredentialResult {
    data class Success(val credential: SocialCredential) : SocialCredentialResult
    /** The user closed the sheet: not an error, nothing to show. */
    data object Cancelled : SocialCredentialResult
    /** This device or build cannot offer the method (no Google account, provider not configured…). */
    data object Unavailable : SocialCredentialResult
    data object Failed : SocialCredentialResult
}

/** After signing in with a provider: in, or the username is still to be chosen (first time). */
sealed interface SocialSignInResult {
    data class SignedIn(val authInfo: AuthInfoModel) : SocialSignInResult
    data class NeedsProfile(val suggestedUsername: String) : SocialSignInResult
}
