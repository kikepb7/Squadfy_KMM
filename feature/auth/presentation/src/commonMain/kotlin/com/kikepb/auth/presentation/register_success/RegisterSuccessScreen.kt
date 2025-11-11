package com.kikepb.auth.presentation.register_success

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kikepb.auth.presentation.register_success.RegisterSuccessAction.OnLoginClick
import com.kikepb.auth.presentation.register_success.RegisterSuccessAction.OnResendVerificationEmailClick
import com.kikepb.core.designsystem.components.buttons.SquadfyButton
import com.kikepb.core.designsystem.components.buttons.SquadfyButtonStyle
import com.kikepb.core.designsystem.components.icons.SquadfySuccessIcon
import com.kikepb.core.designsystem.components.layouts.SquadfyAdaptiveResultLayout
import com.kikepb.core.designsystem.components.layouts.SquadfySimpleSuccessLayout
import com.kikepb.core.designsystem.components.layouts.SquadfySnackbarScaffold
import com.kikepb.core.designsystem.theme.SquadfyTheme
import com.kikepb.core.presentation.util.ObserveAsEvents
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import squadfy_app.feature.auth.presentation.generated.resources.Res.string as RString
import squadfy_app.feature.auth.presentation.generated.resources.account_successfully_created
import squadfy_app.feature.auth.presentation.generated.resources.login
import squadfy_app.feature.auth.presentation.generated.resources.resend_verification_email
import squadfy_app.feature.auth.presentation.generated.resources.resent_verification_email
import squadfy_app.feature.auth.presentation.generated.resources.verification_email_sent_to_x

@Composable
fun RegisterSuccessRoot(
    viewModel: RegisterSuccessViewModel = koinViewModel(),
    onLoginClick: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    ObserveAsEvents(flow = viewModel.events) { event ->
        when (event) {
            is RegisterSuccessEvent.ResentVerificationEmailSuccess -> {
                snackbarHostState.showSnackbar(
                    message = getString(
                        resource = RString.resent_verification_email
                    )
                )
            }
        }
    }

    RegisterSuccessScreen(
        state = state,
        onAction = { action ->
            when (action) {
                is OnLoginClick -> onLoginClick()
                else -> Unit
            }
            viewModel.onAction(action = action)
        },
        snackbarHostState = snackbarHostState
    )
}

@Composable
fun RegisterSuccessScreen(
    state: RegisterSuccessState,
    onAction: (RegisterSuccessAction) -> Unit,
    snackbarHostState: SnackbarHostState
) {
    SquadfySnackbarScaffold(
        snackbarHostState = snackbarHostState
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
                        onClick = { onAction(OnLoginClick) },
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                secondaryButton = {
                    SquadfyButton(
                        text = stringResource(RString.resend_verification_email),
                        onClick = { onAction(OnResendVerificationEmailClick) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !state.isResendingVerificationEmail,
                        isLoading = state.isResendingVerificationEmail,
                        style = SquadfyButtonStyle.SECONDARY
                    )
                },
                secondaryError = state.resendVerificationError?.asString()
            )
        }
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
            onAction = {},
            snackbarHostState = remember { SnackbarHostState() }
        )
    }
}