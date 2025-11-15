package com.kikepb.auth.presentation.forgot_password

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kikepb.auth.presentation.forgot_password.ForgotPasswordAction.OnSubmitClick
import com.kikepb.core.designsystem.components.brand.SquadfyBrandLogo
import com.kikepb.core.designsystem.components.buttons.SquadfyButton
import com.kikepb.core.designsystem.components.layouts.SquadfyAdaptiveFormLayout
import com.kikepb.core.designsystem.components.textfields.SquadfyTextField
import com.kikepb.core.designsystem.theme.SquadfyTheme
import com.kikepb.core.designsystem.theme.extended
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import squadfy_app.feature.auth.presentation.generated.resources.Res.string as RString
import squadfy_app.feature.auth.presentation.generated.resources.email
import squadfy_app.feature.auth.presentation.generated.resources.email_placeholder
import squadfy_app.feature.auth.presentation.generated.resources.forgot_password
import squadfy_app.feature.auth.presentation.generated.resources.forgot_password_email_sent_successfully
import squadfy_app.feature.auth.presentation.generated.resources.submit

@Composable
fun ForgotPasswordRoot(
    viewModel: ForgotPasswordViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ForgotPasswordScreen(
        state = state,
        onAction = viewModel::onAction
    )
}

@Composable
fun ForgotPasswordScreen(
    state: ForgotPasswordState,
    onAction: (ForgotPasswordAction) -> Unit
) {
    SquadfyAdaptiveFormLayout(
        headerText = stringResource(RString.forgot_password),
        errorText = state.errorText?.asString(),
        logo = {
            SquadfyBrandLogo()
        }
    ) {
        SquadfyTextField(
            state = state.emailTextFieldState,
            modifier = Modifier.fillMaxWidth(),
            placeholder = stringResource(RString.email_placeholder),
            title = stringResource(RString.email),
            isError = state.errorText != null,
            supportingText = state.errorText?.asString(),
            keyboardType = KeyboardType.Email,
            singleLine = true
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
        Spacer(modifier = Modifier.height(16.dp))

        if (state.isEmailSentSuccessfully) {
            Text(
                text = stringResource(RString.forgot_password_email_sent_successfully),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.extended.success,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview
@Composable
private fun ForgotPasswordPreview() {
    SquadfyTheme {
        ForgotPasswordScreen(
            state = ForgotPasswordState(),
            onAction = {}
        )
    }
}