package com.kikepb.auth.presentation.reset_password

import com.kikepb.core.designsystem.triangulacion.preview.ScreenPreview
import com.kikepb.core.designsystem.triangulacion.theme.Triangulacion
import com.kikepb.core.designsystem.triangulacion.components.TriIcon
import com.kikepb.core.designsystem.triangulacion.components.TriButton
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kikepb.auth.presentation.reset_password.ResetPasswordAction.OnSubmitClick
import com.kikepb.auth.presentation.reset_password.ResetPasswordAction.OnTogglePasswordVisibilityClick
import com.kikepb.core.designsystem.components.layouts.SquadfyAdaptiveFormLayout
import com.kikepb.core.designsystem.components.layouts.SquadfySnackbarScaffold
import com.kikepb.core.designsystem.components.textfields.SquadfyPasswordTextField
import com.kikepb.core.designsystem.theme.SquadfyTheme
import com.kikepb.core.designsystem.theme.extended
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import squadfy_app.feature.auth.presentation.generated.resources.Res.string as RString
import squadfy_app.feature.auth.presentation.generated.resources.password
import squadfy_app.feature.auth.presentation.generated.resources.password_hint
import squadfy_app.feature.auth.presentation.generated.resources.reset_password_successfully
import squadfy_app.feature.auth.presentation.generated.resources.reset_password_title
import squadfy_app.feature.auth.presentation.generated.resources.set_new_password
import squadfy_app.feature.auth.presentation.generated.resources.submit

@Composable
fun ResetPasswordRoot(
    viewModel: ResetPasswordViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ResetPasswordScreen(
        state = state,
        onAction = viewModel::onAction
    )
}

@Composable
fun ResetPasswordScreen(
    state: ResetPasswordState,
    onAction: (ResetPasswordAction) -> Unit
) {
    SquadfySnackbarScaffold {
        SquadfyAdaptiveFormLayout(
            headerText = stringResource(RString.set_new_password),
            errorText = state.errorText?.asString()
        ) {
            SquadfyPasswordTextField(
                state = state.passwordTextState,
                modifier = Modifier.fillMaxWidth(),
                placeholder = stringResource(RString.password),
                title = stringResource(RString.password),
                supportingText = stringResource(RString.password_hint),
                isPasswordVisible = state.isPasswordVisible,
                onToggleVisibilityClick = {
                    onAction(OnTogglePasswordVisibilityClick)
                },
                leadingIcon = TriIcon.Lock
            )
            Spacer(modifier = Modifier.height(32.dp))
            TriButton(
                text = stringResource(RString.submit),
                onClick = { onAction(OnSubmitClick) },
                modifier = Modifier.fillMaxWidth(),
                enabled = state.canSubmit,
                isLoading = state.isLoading,
                large = true
            )

            if (state.isResetSuccessful) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(RString.reset_password_successfully),
                    style = Triangulacion.type.label,
                    color = Triangulacion.colors.accentText,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// Previews (spec 018): Tiza and Noche with the data of the mockups

@Preview(name = "02d Nueva contraseña · Tiza", widthDp = 393, heightDp = 852)
@Composable
private fun ResetPasswordTizaPreview() = ScreenPreview { ResetPasswordScreen(state = ResetPasswordState(), onAction = {}) }

@Preview(name = "02d Nueva contraseña · Noche", widthDp = 393, heightDp = 852)
@Composable
private fun ResetPasswordNochePreview() = ScreenPreview(darkTheme = true) { ResetPasswordScreen(state = ResetPasswordState(), onAction = {}) }
