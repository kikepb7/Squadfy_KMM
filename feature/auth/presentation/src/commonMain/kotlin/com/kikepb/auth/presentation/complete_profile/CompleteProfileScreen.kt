package com.kikepb.auth.presentation.complete_profile

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kikepb.core.designsystem.components.layouts.SquadfyAdaptiveFormLayout
import com.kikepb.core.designsystem.components.layouts.SquadfySnackbarScaffold
import com.kikepb.core.designsystem.components.textfields.SquadfyTextField
import com.kikepb.core.designsystem.triangulacion.components.TriButton
import com.kikepb.core.designsystem.triangulacion.components.TriIcon
import com.kikepb.core.designsystem.triangulacion.components.TriTextButton
import com.kikepb.core.designsystem.triangulacion.preview.ScreenPreview
import com.kikepb.core.presentation.util.ObserveAsEvents
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import squadfy_app.feature.auth.presentation.generated.resources.Res
import squadfy_app.feature.auth.presentation.generated.resources.complete_profile_cancel
import squadfy_app.feature.auth.presentation.generated.resources.complete_profile_continue
import squadfy_app.feature.auth.presentation.generated.resources.complete_profile_subtitle
import squadfy_app.feature.auth.presentation.generated.resources.complete_profile_title
import squadfy_app.feature.auth.presentation.generated.resources.username
import squadfy_app.feature.auth.presentation.generated.resources.username_hint
import squadfy_app.feature.auth.presentation.generated.resources.username_placeholder

@Composable
fun CompleteProfileRoot(
    suggestedUsername: String,
    onDone: () -> Unit,
    onLeft: () -> Unit,
    viewModel: CompleteProfileViewModel = koinViewModel { parametersOf(suggestedUsername) }
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ObserveAsEvents(flow = viewModel.events) { event ->
        when (event) {
            CompleteProfileEvent.Done -> onDone()
            CompleteProfileEvent.Left -> onLeft()
        }
    }
    CompleteProfileScreen(state = state, onAction = viewModel::onAction)
}

@Composable
fun CompleteProfileScreen(
    state: CompleteProfileState,
    onAction: (CompleteProfileAction) -> Unit
) {
    SquadfySnackbarScaffold {
        SquadfyAdaptiveFormLayout(
            headerText = stringResource(Res.string.complete_profile_title),
            subtitle = stringResource(Res.string.complete_profile_subtitle),
            errorText = state.error?.asString(),
            modifier = Modifier.fillMaxSize()
        ) {
            SquadfyTextField(
                state = state.usernameTextState,
                placeholder = stringResource(Res.string.username_placeholder),
                title = stringResource(Res.string.username),
                supportingText = stringResource(Res.string.username_hint),
                singleLine = true,
                leadingIcon = TriIcon.User,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(32.dp))
            TriButton(
                text = stringResource(Res.string.complete_profile_continue),
                onClick = { onAction(CompleteProfileAction.OnContinueClick) },
                enabled = state.canContinue,
                isLoading = state.isSaving,
                large = true,
                modifier = Modifier.fillMaxWidth()
            )
            TriTextButton(
                text = stringResource(Res.string.complete_profile_cancel),
                onClick = { onAction(CompleteProfileAction.OnCancelClick) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// Previews (spec 018): Tiza and Noche with the default state, no sample data

@Preview(name = "02h Elige tu nombre · Tiza", widthDp = 393, heightDp = 852)
@Composable
private fun CompleteProfileTizaPreview() = ScreenPreview { CompleteProfileScreen(state = CompleteProfileState(), onAction = {}) }

@Preview(name = "02h Elige tu nombre · Noche", widthDp = 393, heightDp = 852)
@Composable
private fun CompleteProfileNochePreview() = ScreenPreview(darkTheme = true) { CompleteProfileScreen(state = CompleteProfileState(), onAction = {}) }
