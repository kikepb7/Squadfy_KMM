package com.kikepb.auth.presentation.register_success

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kikepb.core.designsystem.components.buttons.SquadfyButton
import com.kikepb.core.designsystem.components.buttons.SquadfyButtonStyle
import com.kikepb.core.designsystem.components.icons.SquadfySuccessIcon
import com.kikepb.core.designsystem.components.layouts.SquadfyAdaptiveResultLayout
import com.kikepb.core.designsystem.components.layouts.SquadfySimpleSuccessLayout
import com.kikepb.core.designsystem.theme.SquadfyTheme
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import squadfy_app.feature.auth.presentation.generated.resources.Res.string as RString
import squadfy_app.feature.auth.presentation.generated.resources.account_successfully_created
import squadfy_app.feature.auth.presentation.generated.resources.login
import squadfy_app.feature.auth.presentation.generated.resources.resend_verification_email
import squadfy_app.feature.auth.presentation.generated.resources.verification_email_sent_to_x

@Composable
fun RegisterSuccessRoot(
    viewModel: RegisterSuccessViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    RegisterSuccessScreen(
        state = state,
        onAction = viewModel::onAction
    )
}

@Composable
fun RegisterSuccessScreen(
    state: RegisterSuccessState,
    onAction: (RegisterSuccessAction) -> Unit
) {
    SquadfyAdaptiveResultLayout {
        SquadfySimpleSuccessLayout(
            title = stringResource(RString.account_successfully_created),
            description = stringResource(
                RString.verification_email_sent_to_x,
                state.registeredEmail
            ),
            icon = { SquadfySuccessIcon() },
            primaryButton = {
                SquadfyButton(
                    text = stringResource(RString.login),
                    onClick = { onAction(RegisterSuccessAction.OnLoginClick) },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            secondaryButton = {
                SquadfyButton(
                    text = stringResource(RString.resend_verification_email),
                    onClick = { onAction(RegisterSuccessAction.OnResendVerificationEmailClick) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isResendingVerificationEmail,
                    isLoading = state.isResendingVerificationEmail,
                    style = SquadfyButtonStyle.SECONDARY
                )
            }
        )
    }
}

@Preview
@Composable
private fun RegisterSuccessPreview() {
    SquadfyTheme {
        RegisterSuccessScreen(
            state = RegisterSuccessState(
                registeredEmail = "test@preview.com"
            ),
            onAction = {}
        )
    }
}