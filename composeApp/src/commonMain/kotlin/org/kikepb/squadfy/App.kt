package org.kikepb.squadfy

import androidx.compose.runtime.*
import com.kikepb.core.designsystem.theme.SquadfyTheme
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.kikepb.squadfy.navigation.NavigationRoot

@Composable
@Preview
fun App() {
    SquadfyTheme {
        NavigationRoot()
    }
}