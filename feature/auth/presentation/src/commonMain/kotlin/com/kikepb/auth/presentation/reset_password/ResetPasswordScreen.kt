package com.kikepb.auth.presentation.reset_password

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
import com.kikepb.core.designsystem.components.brand.SquadfyBrandLogo
import com.kikepb.core.designsystem.components.buttons.SquadfyButton
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
            errorText = state.errorText?.asString(),
            logo = {
                SquadfyBrandLogo()
            }
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
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
            SquadfyButton(
                text = stringResource(RString.submit),
                onClick = {
                    onAction(OnSubmitClick)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLoading && state.canSubmit,
                isLoading = state.isLoading
            )

            if (state.isResetSuccessful) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(RString.reset_password_successfully),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.extended.success,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Preview
@Composable
private fun ResetPasswordPreview() {
    SquadfyTheme {
        ResetPasswordScreen(
            state = ResetPasswordState(),
            onAction = {}
        )
    }
}