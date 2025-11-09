package com.kikepb.auth.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import androidx.navigation.navigation
import com.kikepb.auth.presentation.email_verification.EmailVerificationRoot
import com.kikepb.auth.presentation.navigation.AuthGraphRoutes.EmailVerification
import com.kikepb.auth.presentation.navigation.AuthGraphRoutes.Graph
import com.kikepb.auth.presentation.navigation.AuthGraphRoutes.Register
import com.kikepb.auth.presentation.navigation.AuthGraphRoutes.RegisterSuccess
import com.kikepb.auth.presentation.register.RegisterRoot
import com.kikepb.auth.presentation.register_success.RegisterSuccessRoot

fun NavGraphBuilder.authGraph(
    navController: NavController,
    onLoginSuccess: () -> Unit
) {
    navigation<Graph>(
        startDestination = Register
    ) {
        composable<Register> {
            RegisterRoot(
                onRegisterSuccess = { email ->
                    navController.navigate(RegisterSuccess(email = email))
                }
            )
        }
        composable<RegisterSuccess> {
            RegisterSuccessRoot()
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
            EmailVerificationRoot()
        }
    }
}