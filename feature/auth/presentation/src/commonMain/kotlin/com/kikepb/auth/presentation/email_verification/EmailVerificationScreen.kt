package com.kikepb.auth.presentation.email_verification

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kikepb.auth.presentation.email_verification.EmailVerificationAction.OnCloseClick
import com.kikepb.auth.presentation.email_verification.EmailVerificationAction.OnLoginclick
import com.kikepb.core.designsystem.components.brand.SquadfyFailureIcon
import com.kikepb.core.designsystem.components.buttons.SquadfyButton
import com.kikepb.core.designsystem.components.buttons.SquadfyButtonStyle
import com.kikepb.core.designsystem.components.icons.SquadfySuccessIcon
import com.kikepb.core.designsystem.components.layouts.SquadfyAdaptiveResultLayout
import com.kikepb.core.designsystem.components.layouts.SquadfySimpleResultLayout
import com.kikepb.core.designsystem.components.layouts.SquadfySimpleSuccessLayout
import com.kikepb.core.designsystem.components.layouts.SquadfySnackbarScaffold
import com.kikepb.core.designsystem.theme.SquadfyTheme
import com.kikepb.core.designsystem.theme.extended
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import squadfy_app.feature.auth.presentation.generated.resources.Res
import squadfy_app.feature.auth.presentation.generated.resources.close
import squadfy_app.feature.auth.presentation.generated.resources.email_verified_failed
import squadfy_app.feature.auth.presentation.generated.resources.email_verified_failed_desc
import squadfy_app.feature.auth.presentation.generated.resources.email_verified_successfully
import squadfy_app.feature.auth.presentation.generated.resources.email_verified_successfully_desc
import squadfy_app.feature.auth.presentation.generated.resources.login
import squadfy_app.feature.auth.presentation.generated.resources.verifying_account

@Composable
fun EmailVerificationRoot(
    viewModel: EmailVerificationViewModel = koinViewModel(),
    onLoginClick: () -> Unit,
    onCloseClick: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    EmailVerificationScreen(
        state = state,
        onAction = { action ->
            when (action) {
                OnCloseClick -> onCloseClick()
                OnLoginclick -> onLoginClick()
            }
            viewModel.onAction(action = action)
        }
    )
}

@Composable
fun EmailVerificationScreen(
    state: EmailVerificationState,
    onAction: (EmailVerificationAction) -> Unit
) {
    SquadfySnackbarScaffold {
        SquadfyAdaptiveResultLayout {
            when {
                state.isVerifying -> {
                    VerifyingContent(
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                state.isVerified -> {
                    SquadfySimpleSuccessLayout(
                        title = stringResource(Res.string.email_verified_successfully),
                        description = stringResource(Res.string.email_verified_successfully_desc),
                        icon = {
                            SquadfySuccessIcon()
                        },
                        primaryButton = {
                            SquadfyButton(
                                text = stringResource(Res.string.login),
                                onClick = {
                                    onAction(OnLoginclick)
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    )
                }
                else -> {
                    SquadfySimpleResultLayout(
                        title = stringResource(Res.string.email_verified_failed),
                        description = stringResource(Res.string.email_verified_failed_desc),
                        icon = {
                            Spacer(modifier = Modifier.height(32.dp))
                            SquadfyFailureIcon(
                                modifier = Modifier.size(80.dp)
                            )
                            Spacer(modifier = Modifier.height(32.dp))
                        },
                        primaryButton = {
                            SquadfyButton(
                                text = stringResource(Res.string.close),
                                onClick = {
                                    onAction(OnCloseClick)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                style = SquadfyButtonStyle.SECONDARY
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun VerifyingContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .heightIn(min = 200.dp)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(64.dp),
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = stringResource(Res.string.verifying_account),
            color = MaterialTheme.colorScheme.extended.textSecondary,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Preview
@Composable
private fun EmailVerificationErrorPreview() {
    SquadfyTheme {
        EmailVerificationScreen(
            state = EmailVerificationState(),
            onAction = {}
        )
    }
}

@Preview
@Composable
private fun EmailVerificationVerifyingPreview() {
    SquadfyTheme {
        EmailVerificationScreen(
            state = EmailVerificationState(isVerifying = true),
            onAction = {}
        )
    }
}

@Preview
@Composable
private fun EmailVerificationSuccessPreview() {
    SquadfyTheme {
        EmailVerificationScreen(
            state = EmailVerificationState(isVerified = true),
            onAction = {}
        )
    }
}