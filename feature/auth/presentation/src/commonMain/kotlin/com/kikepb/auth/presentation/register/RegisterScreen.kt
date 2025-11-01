package com.kikepb.auth.presentation.register

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kikepb.core.designsystem.components.brand.SquadfyBrandLogo
import com.kikepb.core.designsystem.components.buttons.SquadfyButton
import com.kikepb.core.designsystem.components.buttons.SquadfyButtonStyle
import com.kikepb.core.designsystem.components.layouts.SquadfyAdaptiveFormLayout
import com.kikepb.core.designsystem.components.layouts.SquadfySnackbarScaffold
import com.kikepb.core.designsystem.components.textfields.SquadfyPasswordTextField
import com.kikepb.core.designsystem.components.textfields.SquadfyTextField
import com.kikepb.core.designsystem.theme.SquadfyTheme
import com.kikepb.core.presentation.util.ObserveAsEvents
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import squadfy_app.feature.auth.presentation.generated.resources.Res
import squadfy_app.feature.auth.presentation.generated.resources.email
import squadfy_app.feature.auth.presentation.generated.resources.email_placeholder
import squadfy_app.feature.auth.presentation.generated.resources.login
import squadfy_app.feature.auth.presentation.generated.resources.password
import squadfy_app.feature.auth.presentation.generated.resources.register
import squadfy_app.feature.auth.presentation.generated.resources.username
import squadfy_app.feature.auth.presentation.generated.resources.username_hint
import squadfy_app.feature.auth.presentation.generated.resources.username_placeholder
import squadfy_app.feature.auth.presentation.generated.resources.welcome_to_squadfy

@Composable
fun RegisterRoot(
    viewModel: RegisterViewModel = viewModel(),
    onRegisterSuccess: (String) -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is RegisterEvent.Success -> onRegisterSuccess(event.email)
        }
    }

    RegisterScreen(
        state = state,
        onAction = viewModel::onAction,
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
            errorText = state.registrationError?.asString(),
            logo = { SquadfyBrandLogo() }
        ) {
            SquadfyTextField(
                state = state.usernameTextState,
                placeholder = stringResource(Res.string.username_placeholder),
                title = stringResource(Res.string.username),
                supportingText = state.usernameError?.asString() ?: stringResource(Res.string.username_hint),
                isError = state.usernameError != null,
                onFocusChanged = { isFocused ->
                    onAction(RegisterAction.OnInputTextFocusGain)
                }
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
                }
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
                isPasswordVisible = state.isPasswordVisible
            )
            Spacer(modifier = Modifier.height(16.dp))
            SquadfyButton(
                text = stringResource(Res.string.register),
                onClick = { onAction(RegisterAction.OnRegisterClick) },
                enabled =  state.canRegister,
                isLoading = state.isRegistering,
                modifier = Modifier.fillMaxWidth()
            )

            SquadfyButton(
                text = stringResource(Res.string.login),
                onClick = { onAction(RegisterAction.OnLoginClick) },
                style = SquadfyButtonStyle.SECONDARY,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Preview()
@Composable
private fun RegisterScreenPreview() {
    SquadfyTheme {
        RegisterScreen(
            state = RegisterState(),
            onAction = {},
            snackbarHostState = remember { SnackbarHostState() }
        )
    }
}
