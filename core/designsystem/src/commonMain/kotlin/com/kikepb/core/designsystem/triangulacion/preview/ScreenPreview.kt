package com.kikepb.core.designsystem.triangulacion.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.kikepb.core.designsystem.theme.SquadfyTheme
import com.kikepb.core.designsystem.triangulacion.theme.Triangulacion

/**
 * Frame of the screen previews (spec 018): the app theme, Tiza or Noche, on the screen background. Use it with
 * `@Preview(name = "… · Tiza", widthDp = 393, heightDp = 852)` (and «Noche» with [darkTheme]) so every screen
 * can be checked against its mockup in both themes.
 */
@Composable
fun ScreenPreview(darkTheme: Boolean = false, content: @Composable () -> Unit) {
    SquadfyTheme(darkTheme = darkTheme) {
        Box(modifier = Modifier.fillMaxSize().background(Triangulacion.colors.surface100)) {
            content()
        }
    }
}
