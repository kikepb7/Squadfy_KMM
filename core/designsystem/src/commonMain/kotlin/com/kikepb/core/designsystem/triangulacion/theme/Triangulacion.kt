package com.kikepb.core.designsystem.triangulacion.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable

/** Entry point to the Triangulación tokens inside [com.kikepb.core.designsystem.theme.SquadfyTheme]. */
object Triangulacion {
    val colors: TriangulacionColors
        @Composable @ReadOnlyComposable get() = LocalTriangulacionColors.current
    val type: TriangulacionTypography
        @Composable @ReadOnlyComposable get() = LocalTriangulacionTypography.current
}
