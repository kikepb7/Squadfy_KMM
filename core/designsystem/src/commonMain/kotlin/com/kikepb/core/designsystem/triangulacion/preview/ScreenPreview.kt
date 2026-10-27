package com.kikepb.core.designsystem.triangulacion.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import com.kikepb.core.designsystem.theme.SquadfyTheme
import com.kikepb.core.designsystem.triangulacion.theme.Triangulacion

/**
 * Frame of the screen previews (spec 018): the app theme, Tiza or Noche, on the screen background. Use it with
 * `@Preview(name = "… · Tiza", widthDp = 393, heightDp = 852)` (and «Noche» with [darkTheme]) so every screen
 * can be checked against its mockup in both themes.
 */
@Composable
fun ScreenPreview(darkTheme: Boolean = false, fontScale: Float? = null, content: @Composable () -> Unit) {
    SquadfyTheme(darkTheme = darkTheme) {
        Box(modifier = Modifier.fillMaxSize().background(Triangulacion.colors.surface100)) {
            if (fontScale == null) {
                content()
            } else {
                // Accessibility audit 2026-10: check a screen with the largest system font (Android 14+ allows 200%)
                val density = LocalDensity.current
                CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale)) { content() }
            }
        }
    }
}

/** Font scale of the «· 200 %» previews: the largest Android system size (iOS accessibility sizes go further). */
const val LARGEST_FONT_SCALE = 2f
