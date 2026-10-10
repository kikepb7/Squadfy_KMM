package com.kikepb.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.kikepb.core.designsystem.triangulacion.theme.LocalTriangulacionColors
import com.kikepb.core.designsystem.triangulacion.theme.LocalTriangulacionTypography
import com.kikepb.core.designsystem.triangulacion.theme.NocheColors
import com.kikepb.core.designsystem.triangulacion.theme.TizaColors
import com.kikepb.core.designsystem.triangulacion.theme.TriangulacionTypography

/** Tiza (light, default) or Noche (dark), following the system (spec 018). Wraps Material 3 and Triangulación. */
@Composable
fun SquadfyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val triangulacionType = TriangulacionTypography(display = BricolageGrotesque, sans = InstrumentSans)
    CompositionLocalProvider(
        LocalExtendedColors provides if (darkTheme) DarkExtendedColors else LightExtendedColors,
        LocalTriangulacionColors provides if (darkTheme) NocheColors else TizaColors,
        LocalTriangulacionTypography provides triangulacionType
    ) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
            typography = squadfyTypography(triangulacionType),
            content = content
        )
    }
}
