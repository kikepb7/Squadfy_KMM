package org.kikepb.squadfy

import androidx.compose.runtime.*
import com.kikepb.auth.presentation.register.RegisterRoot
import com.kikepb.core.designsystem.theme.SquadfyTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    SquadfyTheme {
        RegisterRoot(
            onRegisterSuccess = {}
        )
    }
}