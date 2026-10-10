package com.kikepb.auth.presentation.preview

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.kikepb.auth.presentation.email_verification.EmailVerificationScreen
import com.kikepb.auth.presentation.email_verification.EmailVerificationState
import com.kikepb.auth.presentation.forgot_password.ForgotPasswordScreen
import com.kikepb.auth.presentation.forgot_password.ForgotPasswordState
import com.kikepb.auth.presentation.login.LoginScreen
import com.kikepb.auth.presentation.login.LoginState
import com.kikepb.auth.presentation.register.RegisterScreen
import com.kikepb.auth.presentation.register.RegisterState
import com.kikepb.auth.presentation.register_success.RegisterSuccessScreen
import com.kikepb.auth.presentation.register_success.RegisterSuccessState
import com.kikepb.auth.presentation.reset_password.ResetPasswordScreen
import com.kikepb.auth.presentation.reset_password.ResetPasswordState
import com.kikepb.auth.presentation.welcome.WelcomeScreen
import com.kikepb.core.designsystem.triangulacion.preview.ScreenPreview
import org.jetbrains.compose.ui.tooling.preview.Preview

// Spec 018: every auth screen in Tiza and Noche (mockups 01-bienvenida and 02-login).

@Preview(name = "01 Bienvenida · Tiza", widthDp = 393, heightDp = 852)
@Composable
private fun WelcomeTiza() = ScreenPreview { WelcomeScreen(onCreateAccountClick = {}, onLoginClick = {}) }

@Preview(name = "01 Bienvenida · Noche", widthDp = 393, heightDp = 852)
@Composable
private fun WelcomeNoche() = ScreenPreview(darkTheme = true) { WelcomeScreen(onCreateAccountClick = {}, onLoginClick = {}) }

@Preview(name = "02 Login · Tiza", widthDp = 393, heightDp = 852)
@Composable
private fun LoginTiza() = ScreenPreview { LoginScreen(state = LoginState(), onAction = {}) }

@Preview(name = "02 Login · Noche", widthDp = 393, heightDp = 852)
@Composable
private fun LoginNoche() = ScreenPreview(darkTheme = true) { LoginScreen(state = LoginState(), onAction = {}) }

@Preview(name = "02b Registro · Tiza", widthDp = 393, heightDp = 852)
@Composable
private fun RegisterTiza() = ScreenPreview { RegisterScreen(state = RegisterState(), onAction = {}, snackbarHostState = remember { SnackbarHostState() }) }

@Preview(name = "02b Registro · Noche", widthDp = 393, heightDp = 852)
@Composable
private fun RegisterNoche() = ScreenPreview(darkTheme = true) { RegisterScreen(state = RegisterState(), onAction = {}, snackbarHostState = remember { SnackbarHostState() }) }

@Preview(name = "02c Olvidé la contraseña · Tiza", widthDp = 393, heightDp = 852)
@Composable
private fun ForgotTiza() = ScreenPreview { ForgotPasswordScreen(state = ForgotPasswordState(), onAction = {}) }

@Preview(name = "02c Olvidé la contraseña · Noche", widthDp = 393, heightDp = 852)
@Composable
private fun ForgotNoche() = ScreenPreview(darkTheme = true) { ForgotPasswordScreen(state = ForgotPasswordState(), onAction = {}) }

@Preview(name = "02d Nueva contraseña · Tiza", widthDp = 393, heightDp = 852)
@Composable
private fun ResetTiza() = ScreenPreview { ResetPasswordScreen(state = ResetPasswordState(), onAction = {}) }

@Preview(name = "02d Nueva contraseña · Noche", widthDp = 393, heightDp = 852)
@Composable
private fun ResetNoche() = ScreenPreview(darkTheme = true) { ResetPasswordScreen(state = ResetPasswordState(), onAction = {}) }

@Preview(name = "02e Cuenta creada · Tiza", widthDp = 393, heightDp = 852)
@Composable
private fun RegisterSuccessTiza() = ScreenPreview {
    RegisterSuccessScreen(state = RegisterSuccessState(registeredEmail = "alvaro@correo.es"), onAction = {}, snackbarHostState = remember { SnackbarHostState() })
}

@Preview(name = "02e Cuenta creada · Noche", widthDp = 393, heightDp = 852)
@Composable
private fun RegisterSuccessNoche() = ScreenPreview(darkTheme = true) {
    RegisterSuccessScreen(state = RegisterSuccessState(registeredEmail = "alvaro@correo.es"), onAction = {}, snackbarHostState = remember { SnackbarHostState() })
}

@Preview(name = "02f Email verificado · Tiza", widthDp = 393, heightDp = 852)
@Composable
private fun EmailVerifiedTiza() = ScreenPreview { EmailVerificationScreen(state = EmailVerificationState(isVerified = true), onAction = {}) }

@Preview(name = "02f Email verificado · Noche", widthDp = 393, heightDp = 852)
@Composable
private fun EmailVerifiedNoche() = ScreenPreview(darkTheme = true) { EmailVerificationScreen(state = EmailVerificationState(isVerified = true), onAction = {}) }
