package com.kikepb.auth.presentation.register

import com.kikepb.core.designsystem.triangulacion.preview.ScreenPreview
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import com.kikepb.core.designsystem.triangulacion.components.TriButton
import com.kikepb.core.designsystem.triangulacion.components.TriIcon
import com.kikepb.core.designsystem.triangulacion.components.TriTextButton
import com.kikepb.core.designsystem.triangulacion.theme.Triangulacion
import squadfy_app.feature.auth.presentation.generated.resources.have_account_prefix
import squadfy_app.feature.auth.presentation.generated.resources.login_link
import squadfy_app.feature.auth.presentation.generated.resources.register_subtitle
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import com.kikepb.core.domain.config.LegalLinks
import org.koin.compose.koinInject
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kikepb.core.designsystem.components.layouts.SquadfyAdaptiveFormLayout
import com.kikepb.core.designsystem.components.layouts.SquadfySnackbarScaffold
import com.kikepb.core.designsystem.components.textfields.SquadfyPasswordTextField
import com.kikepb.core.designsystem.components.textfields.SquadfyTextField
import com.kikepb.core.designsystem.theme.SquadfyTheme
import com.kikepb.core.presentation.util.ObserveAsEvents
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import squadfy_app.feature.auth.presentation.generated.resources.Res
import squadfy_app.feature.auth.presentation.generated.resources.email
import squadfy_app.feature.auth.presentation.generated.resources.email_placeholder
import squadfy_app.feature.auth.presentation.generated.resources.login
import squadfy_app.feature.auth.presentation.generated.resources.privacy_policy
import squadfy_app.feature.auth.presentation.generated.resources.password
import squadfy_app.feature.auth.presentation.generated.resources.register
import squadfy_app.feature.auth.presentation.generated.resources.username
import squadfy_app.feature.auth.presentation.generated.resources.username_hint
import squadfy_app.feature.auth.presentation.generated.resources.username_placeholder
import squadfy_app.feature.auth.presentation.generated.resources.welcome_to_squadfy

@Composable
fun RegisterRoot(
    viewModel: RegisterViewModel = koinViewModel(),
    onRegisterSuccess: (email: String, alreadyVerified: Boolean) -> Unit,
    onLoginClick: () -> Unit,
    legalLinks: LegalLinks = koinInject()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val uriHandler = LocalUriHandler.current
    val snackbarHostState = remember { SnackbarHostState() }

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is RegisterEvent.Success -> onRegisterSuccess(event.email, event.alreadyVerified)
        }
    }

    RegisterScreen(
        state = state,
        onAction = { action ->
            when (action) {
                RegisterAction.OnLoginClick -> onLoginClick()
                RegisterAction.OnPrivacyPolicyClick -> uriHandler.openUri(legalLinks.privacyPolicyUrl)
                else -> Unit
            }
            viewModel.onAction(action = action)
        },
        snackbarHostState = snackbarHostState
    )
}

@Composable
fun RegisterScreen(
    state: RegisterState,
    onAction: (RegisterAction) -> Unit,
    snackbarHostState: SnackbarHostState
) {
    SquadfySnackbarScaffold(snackbarHostState = snackbarHostState) {
        SquadfyAdaptiveFormLayout(
            headerText = stringResource(Res.string.welcome_to_squadfy),
            subtitle = stringResource(Res.string.register_subtitle),
            errorText = state.registrationError?.asString()
        ) {
            SquadfyTextField(
                state = state.usernameTextState,
                placeholder = stringResource(Res.string.username_placeholder),
                title = stringResource(Res.string.username),
                supportingText = state.usernameError?.asString() ?: stringResource(Res.string.username_hint),
                isError = state.usernameError != null,
                onFocusChanged = { isFocused ->
                    onAction(RegisterAction.OnInputTextFocusGain)
                },
                leadingIcon = TriIcon.User
            )
            Spacer(modifier = Modifier.height(16.dp))
            SquadfyTextField(
                state = state.emailTextState,
                placeholder = stringResource(Res.string.email_placeholder),
                title = stringResource(Res.string.email),
                supportingText = state.emailError?.asString(),
                isError = state.emailError != null,
                onFocusChanged = { isFocused ->
                    onAction(RegisterAction.OnInputTextFocusGain)
                },
                keyboardType = KeyboardType.Email,
                leadingIcon = TriIcon.Mail
            )
            Spacer(modifier = Modifier.height(16.dp))
            SquadfyPasswordTextField(
                state = state.passwordTextState,
                placeholder = stringResource(Res.string.password),
                title = stringResource(Res.string.password),
                isError = state.passwordError != null,
                onFocusChanged = { isFocused ->
                    onAction(RegisterAction.OnInputTextFocusGain)
                },
                onToggleVisibilityClick = {
                    onAction(RegisterAction.OnTogglePasswordVisibilityClick)
                },
                isPasswordVisible = state.isPasswordVisible,
                leadingIcon = TriIcon.Lock
            )
            Spacer(modifier = Modifier.height(32.dp))
            TriButton(
                text = stringResource(Res.string.register),
                onClick = { onAction(RegisterAction.OnRegisterClick) },
                enabled = state.canRegister,
                isLoading = state.isRegistering,
                large = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(Res.string.have_account_prefix),
                    style = Triangulacion.type.body,
                    color = Triangulacion.colors.inkMuted
                )
                TriTextButton(text = stringResource(Res.string.login_link), onClick = { onAction(RegisterAction.OnLoginClick) })
            }
            // Spec 011 AC-011-08: the privacy policy is reachable before creating the account
            TriTextButton(
                text = stringResource(Res.string.privacy_policy),
                onClick = { onAction(RegisterAction.OnPrivacyPolicyClick) },
                color = Triangulacion.colors.inkMuted,
                underline = true,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

// Previews (spec 018): Tiza and Noche with the data of the mockups

@Preview(name = "02b Registro · Tiza", widthDp = 393, heightDp = 852)
@Composable
private fun RegisterTizaPreview() = ScreenPreview { RegisterScreen(state = RegisterState(), onAction = {}, snackbarHostState = remember { SnackbarHostState() }) }

@Preview(name = "02b Registro · Noche", widthDp = 393, heightDp = 852)
@Composable
private fun RegisterNochePreview() = ScreenPreview(darkTheme = true) { RegisterScreen(state = RegisterState(), onAction = {}, snackbarHostState = remember { SnackbarHostState() }) }
