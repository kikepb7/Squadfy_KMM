package org.kikepb.squadfy.auth

/**
 * Implemented in Swift (iosApp/iosApp/SocialSignIn.swift): the sign-in sheets need UIKit, AuthenticationServices and
 * the Google Sign-In SDK. Every callback is called exactly once. Only Swift-friendly types cross this boundary.
 */
interface SocialSignInBridge {
    fun signInWithGoogle(onResult: (SocialSignInOutcome) -> Unit)
    fun signInWithApple(onResult: (SocialSignInOutcome) -> Unit)
    /** Revokes the Sign in with Apple tokens of the signed-in user; [onDone] gets the error message, if any. */
    fun revokeAppleToken(authorizationCode: String, onDone: (String?) -> Unit)
}

/** What a sign-in sheet ended with. Swift builds it with the companion's factories. */
class SocialSignInOutcome private constructor(
    val status: Status,
    val idToken: String? = null,
    val rawNonce: String? = null,
    val authorizationCode: String? = null,
    val givenName: String? = null,
    val familyName: String? = null
) {
    enum class Status { SUCCESS, CANCELLED, UNAVAILABLE, FAILED }

    companion object {
        fun google(idToken: String) = SocialSignInOutcome(Status.SUCCESS, idToken = idToken)
        fun apple(idToken: String, rawNonce: String, authorizationCode: String?, givenName: String?, familyName: String?) =
            SocialSignInOutcome(Status.SUCCESS, idToken, rawNonce, authorizationCode, givenName, familyName)
        fun cancelled() = SocialSignInOutcome(Status.CANCELLED)
        fun unavailable() = SocialSignInOutcome(Status.UNAVAILABLE)
        fun failed() = SocialSignInOutcome(Status.FAILED)
    }
}

/** Where Swift registers its bridge at launch, before Koin starts. Exposed to Swift as `IosSocialSignIn.shared`. */
object IosSocialSignIn {
    var bridge: SocialSignInBridge? = null
}
