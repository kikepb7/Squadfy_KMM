package com.kikepb.auth.presentation.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import androidx.navigation.navigation
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import com.kikepb.auth.presentation.email_verification.EmailVerificationRoot
import com.kikepb.auth.presentation.forgot_password.ForgotPasswordRoot
import com.kikepb.auth.presentation.login.LoginRoot
import com.kikepb.auth.presentation.navigation.AuthGraphRoutes.EmailVerification
import com.kikepb.auth.presentation.navigation.AuthGraphRoutes.ForgotPassword
import com.kikepb.auth.presentation.navigation.AuthGraphRoutes.Graph
import com.kikepb.auth.presentation.navigation.AuthGraphRoutes.Login
import com.kikepb.auth.presentation.navigation.AuthGraphRoutes.Register
import com.kikepb.auth.presentation.navigation.AuthGraphRoutes.RegisterSuccess
import com.kikepb.auth.presentation.navigation.AuthGraphRoutes.ResetPassword
import com.kikepb.auth.presentation.register.RegisterRoot
import com.kikepb.auth.presentation.register_success.RegisterSuccessRoot
import com.kikepb.auth.presentation.reset_password.ResetPasswordRoot
import com.kikepb.core.presentation.navigation.navigateTo
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

@Composable
fun AuthGraphNavigation(
    onLoginSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val authBackStack = rememberNavBackStack(
        configuration = SavedStateConfiguration {
            serializersModule = SerializersModule {
                polymorphic(NavKey::class) {
                    subclass(Login::class, Login.serializer())
                    subclass(Register::class, Register.serializer())
                    subclass(RegisterSuccess::class, RegisterSuccess.serializer())
                    subclass(EmailVerification::class, EmailVerification.serializer())
                    subclass(ForgotPassword::class, ForgotPassword.serializer())
                    subclass(ResetPassword::class, ResetPassword.serializer())
                }
            }
        },
        Login
    )

    NavDisplay(
        backStack = authBackStack,
        modifier = modifier,
        entryProvider = entryProvider {
            entry<Login> {
                LoginRoot(
                    onLoginSuccess = onLoginSuccess,
                    onForgotPasswordClick = {
                        authBackStack.navigateTo(screen = ForgotPassword)
                    },
                    onCreateAccountClick = {
                        authBackStack.navigateTo(screen = Register)
                        /*navController.navigate(route = Register) {
                            restoreState = true
                            launchSingleTop = true
                        }*/
                    }
                )
            }
            entry<Register> {
                RegisterRoot(
                    onRegisterSuccess = { email ->
                        authBackStack.navigateTo(screen = RegisterSuccess(email = email))
                    },
                    onLoginClick = {
                        authBackStack.remove(element = Register)
                        authBackStack.navigateTo(screen = Login)
                        /*navController.navigate(route = Login) {
                            popUpTo(route = Register) {
                                inclusive = true
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }*/
                    }
                )
            }
            entry<RegisterSuccess> {
                RegisterSuccessRoot(
                    onLoginClick = {
                        authBackStack.remove(element = RegisterSuccess(email = it.email))
                        authBackStack.navigateTo(screen = Login)
                        /*navController.navigate(Login) {
                            popUpTo<RegisterSuccess> {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }*/
                    }
                )
            }
            entry<EmailVerification> {
                EmailVerificationRoot(
                    onLoginClick = {
                        authBackStack.remove(element = EmailVerification(token = it.token))
                        authBackStack.navigateTo(screen = Login)
                       /* navController.navigate(Login) {
                            popUpTo<EmailVerification> {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }*/
                    },
                    onCloseClick = {
                        authBackStack.remove(element = EmailVerification(token = it.token))
                        authBackStack.navigateTo(screen = Login)
                        /*navController.navigate(Login) {
                            popUpTo<EmailVerification> {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }*/
                    }
                )
            }
            entry<ForgotPassword> {
                ForgotPasswordRoot()
            }
            entry<ResetPassword> {
                ResetPasswordRoot()
            }
        },
        transitionSpec = {
            slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = tween(250)
            ) togetherWith slideOutHorizontally(
                targetOffsetX = { -it },
                animationSpec = tween(250)
            )
        },
        popTransitionSpec = {
            slideInHorizontally(
                initialOffsetX = { -it },
                animationSpec = tween(250)
            ) togetherWith slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = tween(250)
            )
        },
        predictivePopTransitionSpec = {
            slideInHorizontally(
                initialOffsetX = { -it },
                animationSpec = tween(250)
            ) togetherWith slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = tween(250)
            )
        }
    )
}


/*
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
}*/
