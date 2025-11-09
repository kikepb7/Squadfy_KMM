package org.kikepb.squadfy

import androidx.compose.runtime.*
import androidx.navigation.compose.rememberNavController
import com.kikepb.core.designsystem.theme.SquadfyTheme
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.kikepb.squadfy.navigation.DeepLinkListener
import org.kikepb.squadfy.navigation.NavigationRoot

@Composable
@Preview
fun App() {
    val navController = rememberNavController()
    DeepLinkListener(navController = navController)

    SquadfyTheme {
        NavigationRoot(navController = navController)
    }
}