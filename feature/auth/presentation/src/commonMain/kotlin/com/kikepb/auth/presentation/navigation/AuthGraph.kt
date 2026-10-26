package com.kikepb.auth.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import androidx.navigation.navDeepLink
import androidx.navigation.navigation
import com.kikepb.auth.presentation.email_verification.EmailVerificationRoot
import com.kikepb.auth.presentation.forgot_password.ForgotPasswordRoot
import com.kikepb.auth.presentation.login.LoginRoot
import com.kikepb.auth.presentation.navigation.AuthGraphRoutes.EmailVerification
import com.kikepb.auth.presentation.navigation.AuthGraphRoutes.ForgotPassword
import com.kikepb.auth.presentation.navigation.AuthGraphRoutes.AuthGraph
import com.kikepb.auth.presentation.navigation.AuthGraphRoutes.Login
import com.kikepb.auth.presentation.navigation.AuthGraphRoutes.Welcome
import com.kikepb.auth.presentation.navigation.AuthGraphRoutes.Register
import com.kikepb.auth.presentation.navigation.AuthGraphRoutes.RegisterSuccess
import com.kikepb.auth.presentation.navigation.AuthGraphRoutes.ResetPassword
import com.kikepb.auth.presentation.register.RegisterRoot
import com.kikepb.auth.presentation.complete_profile.CompleteProfileRoot
import com.kikepb.auth.presentation.navigation.AuthGraphRoutes.CompleteProfile
import com.kikepb.auth.presentation.welcome.WelcomeRoot
import com.kikepb.auth.presentation.register_success.RegisterSuccessRoot
import com.kikepb.auth.presentation.reset_password.ResetPasswordRoot

fun NavGraphBuilder.authGraph(
    navController: NavController,
    onLoginSuccess: () -> Unit
) {
    navigation<AuthGraph>(
        startDestination = Welcome
    ) {
        composable<Welcome> {
            WelcomeRoot(
                onCreateAccountClick = { navController.navigate(route = Register) { launchSingleTop = true } },
                onLoginClick = { navController.navigate(route = Login) { launchSingleTop = true } },
                onSignedIn = onLoginSuccess,
                onNeedsProfile = { suggested -> navController.navigate(route = CompleteProfile(suggested)) { launchSingleTop = true } }
            )
        }
        composable<CompleteProfile> { entry ->
            CompleteProfileRoot(
                suggestedUsername = entry.toRoute<CompleteProfile>().suggestedUsername,
                onDone = onLoginSuccess,
                onLeft = { navController.popBackStack() }
            )
        }
        composable<Login> {
            LoginRoot(
                onLoginSuccess = onLoginSuccess,
                onNeedsProfile = { suggested -> navController.navigate(route = CompleteProfile(suggested)) { launchSingleTop = true } },
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
                onRegisterSuccess = { email, alreadyVerified ->
                    navController.navigate(route = RegisterSuccess(email = email, alreadyVerified = alreadyVerified))
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
        composable<ForgotPassword> {
            ForgotPasswordRoot(onNavigateBack = { navController.popBackStack() })
        }
        composable<ResetPassword>(
            deepLinks = listOf(
                // Backend v1 emails RESET_PASSWORD_URL?token=..., by default squadfy://reset-password (BACKEND.md §6)
                navDeepLink {
                    this.uriPattern = "squadfy://reset-password?token={token}"
                }
            )
        ) {
            ResetPasswordRoot()
        }
    }
}