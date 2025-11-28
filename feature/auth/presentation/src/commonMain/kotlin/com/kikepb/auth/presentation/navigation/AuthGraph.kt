package com.kikepb.auth.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import androidx.navigation.navigation
import com.kikepb.auth.presentation.email_verification.EmailVerificationRoot
import com.kikepb.auth.presentation.forgot_password.ForgotPasswordRoot
import com.kikepb.auth.presentation.login.LoginRoot
import com.kikepb.auth.presentation.navigation.AuthGraphRoutes.EmailVerification
import com.kikepb.auth.presentation.navigation.AuthGraphRoutes.ForgotPassword
import com.kikepb.auth.presentation.navigation.AuthGraphRoutes.AuthGraph
import com.kikepb.auth.presentation.navigation.AuthGraphRoutes.Login
import com.kikepb.auth.presentation.navigation.AuthGraphRoutes.Register
import com.kikepb.auth.presentation.navigation.AuthGraphRoutes.RegisterSuccess
import com.kikepb.auth.presentation.navigation.AuthGraphRoutes.ResetPassword
import com.kikepb.auth.presentation.register.RegisterRoot
import com.kikepb.auth.presentation.register_success.RegisterSuccessRoot
import com.kikepb.auth.presentation.reset_password.ResetPasswordRoot

fun NavGraphBuilder.authGraph(
    navController: NavController,
    onLoginSuccess: () -> Unit
) {
    navigation<AuthGraph>(
        startDestination = Login
    ) {
        composable<Login> {
            LoginRoot(
                onLoginSuccess = onLoginSuccess,
                onForgotPasswordClick = {
                    navController.navigate(route = ForgotPassword)
                },
                onCreateAccountClick = {
                    navController.navigate(route = Register) {
                        restoreState = true
                        launchSingleTop = true
                    }
                }
            )
        }
        composable<Register> {
            RegisterRoot(
                onRegisterSuccess = { email ->
                    navController.navigate(route = RegisterSuccess(email = email))
                },
                onLoginClick = {
                    navController.navigate(route = Login) {
                        popUpTo(route = Register) {
                            inclusive = true
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
        composable<RegisterSuccess> {
            RegisterSuccessRoot(
                onLoginClick = {
                    navController.navigate(Login) {
                        popUpTo<RegisterSuccess> {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            )
        }
        composable< EmailVerification>(
            deepLinks = listOf(
                navDeepLink {
                    this.uriPattern = "https://squadfy.com/api/auth/verify?token={token}"
                },
                navDeepLink {
                    this.uriPattern = "squadfy://squadfy.com/api/auth/verify?token={token}"
                }
            )
        ) {
            EmailVerificationRoot(
                onLoginClick = {
                    navController.navigate(Login) {
                        popUpTo<EmailVerification> {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                },
                onCloseClick = {
                    navController.navigate(Login) {
                        popUpTo<EmailVerification> {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            )
        }
        composable< ForgotPassword> {
            ForgotPasswordRoot()
        }
        composable<ResetPassword>(
            deepLinks = listOf(
                navDeepLink {
                    this.uriPattern = "https://squadfy.com/api/auth/reset-password?token={token}"
                },
                navDeepLink {
                    this.uriPattern = "squadfy://squadfy.com/api/auth/reset-password?token={token}"
                }
            )
        ) {
            ResetPasswordRoot()
        }
    }
}