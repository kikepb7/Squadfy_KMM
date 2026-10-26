package com.kikepb.auth.presentation.login

import com.kikepb.core.designsystem.triangulacion.preview.ScreenPreview
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kikepb.core.designsystem.components.layouts.SquadfyAdaptiveFormLayout
import com.kikepb.core.designsystem.components.layouts.SquadfySnackbarScaffold
import com.kikepb.core.designsystem.components.textfields.SquadfyPasswordTextField
import com.kikepb.core.designsystem.components.textfields.SquadfyTextField
import com.kikepb.core.designsystem.theme.SquadfyTheme
import com.kikepb.core.designsystem.triangulacion.components.TriButton
import com.kikepb.core.designsystem.triangulacion.components.TriIcon
import com.kikepb.core.designsystem.triangulacion.components.TriNotice
import com.kikepb.core.designsystem.triangulacion.components.TriNoticeTone
import com.kikepb.core.designsystem.triangulacion.components.TriTextButton
import com.kikepb.core.designsystem.triangulacion.theme.Triangulacion
import squadfy_app.feature.auth.presentation.generated.resources.login_subtitle
import squadfy_app.feature.auth.presentation.generated.resources.no_account_prefix
import squadfy_app.feature.auth.presentation.generated.resources.register_link
import com.kikepb.core.presentation.util.ObserveAsEvents
import com.kikepb.auth.presentation.social.SocialSignInAction
import com.kikepb.auth.presentation.social.SocialSignInButtons
import com.kikepb.auth.presentation.social.SocialSignInEvent
import com.kikepb.auth.presentation.social.SocialSignInState
import com.kikepb.auth.presentation.social.SocialSignInViewModel
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import squadfy_app.feature.auth.presentation.generated.resources.Res
import squadfy_app.feature.auth.presentation.generated.resources.resend_verification_email
import squadfy_app.feature.auth.presentation.generated.resources.email
import squadfy_app.feature.auth.presentation.generated.resources.email_placeholder
import squadfy_app.feature.auth.presentation.generated.resources.forgot_password
import squadfy_app.feature.auth.presentation.generated.resources.login
import squadfy_app.feature.auth.presentation.generated.resources.password
import squadfy_app.feature.auth.presentation.generated.resources.welcome_back

@Composable
fun LoginRoot(
    viewModel: LoginViewModel = koinViewModel(),
    socialViewModel: SocialSignInViewModel = koinViewModel(),
    onLoginSuccess: () -> Unit,
    onNeedsProfile: (suggestedUsername: String) -> Unit,
    onForgotPasswordClick: () -> Unit,
    onCreateAccountClick: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val socialState by socialViewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(flow = viewModel.events) { event ->
        when (event) {
            LoginEvent.Success -> onLoginSuccess()
        }
    }
    ObserveAsEvents(flow = socialViewModel.events) { event ->
        when (event) {
            SocialSignInEvent.SignedIn -> onLoginSuccess()
            is SocialSignInEvent.NeedsProfile -> onNeedsProfile(event.suggestedUsername)
        }
    }

    LoginScreen(
        state = state,
        socialState = socialState,
        onSocialAction = socialViewModel::onAction,
        onAction = { action ->
            when (action) {
                LoginAction.OnForgotPasswordClick -> onForgotPasswordClick()
                LoginAction.OnSignUpClick -> onCreateAccountClick()
                else -> Unit
            }
            viewModel.onAction(action = action)
        }
    )
}

@Composable
fun LoginScreen(
    state: LoginState,
    onAction: (LoginAction) -> Unit,
    socialState: SocialSignInState = SocialSignInState(),
    onSocialAction: (SocialSignInAction) -> Unit = {}
) {
    SquadfySnackbarScaffold {
        SquadfyAdaptiveFormLayout(
            headerText = stringResource(Res.string.welcome_back),
            subtitle = stringResource(Res.string.login_subtitle),
            errorText = state.error?.asString(),
            modifier = Modifier.fillMaxSize()
        ) {
            SquadfyTextField(
                state = state.emailTextFieldState,
                placeholder = stringResource(Res.string.email_placeholder),
                keyboardType = KeyboardType.Email,
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                title = stringResource(Res.string.email),
                leadingIcon = TriIcon.Mail
            )
            Spacer(modifier = Modifier.height(20.dp))
            SquadfyPasswordTextField(
                state = state.passwordTextFieldState,
                placeholder = stringResource(Res.string.password),
                isPasswordVisible = state.isPasswordVisible,
                onToggleVisibilityClick = {
                    onAction(LoginAction.OnTogglePasswordVisibility)
                },
                modifier = Modifier.fillMaxWidth(),
                title = stringResource(Res.string.password),
                leadingIcon = TriIcon.Lock
            )
            TriTextButton(
                text = stringResource(Res.string.forgot_password),
                onClick = { onAction(LoginAction.OnForgotPasswordClick) },
                modifier = Modifier.align(Alignment.End)
            )
            Spacer(modifier = Modifier.height(32.dp))
            TriButton(
                text = stringResource(Res.string.login),
                onClick = { onAction(LoginAction.OnLoginClick) },
                enabled = state.canLogin,
                isLoading = state.isLoggingIn,
                large = true,
                modifier = Modifier.fillMaxWidth()
            )
            if (state.canResendVerification) {
                TriTextButton(
                    text = stringResource(Res.string.resend_verification_email),
                    onClick = { onAction(LoginAction.OnResendVerificationClick) },
                    enabled = !state.isResendingVerification,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
            // Spec 019 AC-019-23: Google and Apple, when the platform offers them
            SocialSignInButtons(state = socialState, onAction = onSocialAction, modifier = Modifier.padding(top = 16.dp))
            state.info?.let { info ->
                Spacer(modifier = Modifier.height(8.dp))
                TriNotice(title = null, body = info.asString(), tone = TriNoticeTone.Ok)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(Res.string.no_account_prefix),
                    style = Triangulacion.type.body,
                    color = Triangulacion.colors.inkMuted
                )
                TriTextButton(
                    text = stringResource(Res.string.register_link),
                    onClick = { onAction(LoginAction.OnSignUpClick) },
                    modifier = Modifier.padding(start = 0.dp)
                )
            }
        }
    }
}

// Previews (spec 018): Tiza and Noche with the default state, no sample data

@Preview(name = "02 Login · Tiza", widthDp = 393, heightDp = 852)
@Composable
private fun LoginTizaPreview() = ScreenPreview { LoginScreen(state = LoginState(), onAction = {}) }

@Preview(name = "02 Login · Noche", widthDp = 393, heightDp = 852)
@Composable
private fun LoginNochePreview() = ScreenPreview(darkTheme = true) { LoginScreen(state = LoginState(), onAction = {}) }
