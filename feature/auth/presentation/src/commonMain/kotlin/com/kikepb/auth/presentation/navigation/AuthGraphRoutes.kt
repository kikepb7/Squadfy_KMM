package com.kikepb.auth.presentation.navigation

import kotlinx.serialization.Serializable

sealed interface AuthGraphRoutes {
    @Serializable
    data object AuthGraph: AuthGraphRoutes

    /** Spec 018: first screen of the auth flow. */
    @Serializable
    data object Welcome: AuthGraphRoutes

    @Serializable
    data object Login: AuthGraphRoutes

    @Serializable
    data object Register: AuthGraphRoutes

    @Serializable
    data class RegisterSuccess(val email: String, val alreadyVerified: Boolean = false): AuthGraphRoutes

    @Serializable
    data object ForgotPassword: AuthGraphRoutes

    @Serializable
    data class ResetPassword(val token: String): AuthGraphRoutes

    @Serializable
    data class EmailVerification(val token: String): AuthGraphRoutes

    /** Spec 019: the username step of a first Google/Apple sign-in. */
    @Serializable
    data class CompleteProfile(val suggestedUsername: String): AuthGraphRoutes
}