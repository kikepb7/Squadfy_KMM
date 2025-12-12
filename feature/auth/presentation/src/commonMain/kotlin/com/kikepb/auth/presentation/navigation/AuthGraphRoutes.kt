package com.kikepb.auth.presentation.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface AuthGraphRoutes : NavKey {
    @Serializable
    data object Graph: AuthGraphRoutes, NavKey

    @Serializable
    data object Login: AuthGraphRoutes, NavKey

    @Serializable
    data object Register: AuthGraphRoutes, NavKey

    @Serializable
    data class RegisterSuccess(val email: String): AuthGraphRoutes, NavKey

    @Serializable
    data object ForgotPassword: AuthGraphRoutes, NavKey

    @Serializable
    data class ResetPassword(val token: String): AuthGraphRoutes, NavKey

    @Serializable
    data class EmailVerification(val token: String): AuthGraphRoutes, NavKey
}