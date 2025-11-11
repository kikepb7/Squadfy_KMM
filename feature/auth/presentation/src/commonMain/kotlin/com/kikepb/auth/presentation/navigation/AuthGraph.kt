package com.kikepb.auth.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import androidx.navigation.navigation
import com.kikepb.auth.presentation.email_verification.EmailVerificationRoot
import com.kikepb.auth.presentation.login.LoginRoot
import com.kikepb.auth.presentation.navigation.AuthGraphRoutes.EmailVerification
import com.kikepb.auth.presentation.navigation.AuthGraphRoutes.ForgotPassword
import com.kikepb.auth.presentation.navigation.AuthGraphRoutes.Graph
import com.kikepb.auth.presentation.navigation.AuthGraphRoutes.Login
import com.kikepb.auth.presentation.navigation.AuthGraphRoutes.Register
import com.kikepb.auth.presentation.navigation.AuthGraphRoutes.RegisterSuccess
import com.kikepb.auth.presentation.register.RegisterRoot
import com.kikepb.auth.presentation.register_success.RegisterSuccessRoot

fun NavGraphBuilder.authGraph(
    navController: NavController,
    onLoginSuccess: () -> Unit
) {
    navigation<Graph>(
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
    }
}