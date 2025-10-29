package com.kikepb.auth.presentation.register

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kikepb.core.designsystem.theme.SquadfyTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun RegisterRoot(
    viewModel: RegisterViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    RegisterScreen(
        state = state,
        onAction = viewModel::onAction
    )
}

@Composable
fun RegisterScreen(
    state: RegisterState,
    onAction: (RegisterAction) -> Unit
) {
    // TODO: Implement UI for multiplatform register screen
}

@Preview()
@Composable
private fun RegisterScreenPreview() {
    SquadfyTheme {
        RegisterScreen(
            state = RegisterState(),
            onAction = {}
        )
    }
}
