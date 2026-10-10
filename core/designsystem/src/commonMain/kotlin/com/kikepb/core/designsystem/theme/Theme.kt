package com.kikepb.core.designsystem.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.kikepb.core.designsystem.triangulacion.theme.NocheColors
import com.kikepb.core.designsystem.triangulacion.theme.TizaColors
import com.kikepb.core.designsystem.triangulacion.theme.TriangulacionColors

val LocalExtendedColors = staticCompositionLocalOf { LightExtendedColors }

val ColorScheme.extended: ExtendedColors
    @ReadOnlyComposable
    @Composable
    get() = LocalExtendedColors.current

/**
 * Roles the app used before Triangulación (spec 018), now mapped to its tokens so every screen follows Tiza and
 * Noche. New code reads `Triangulacion.colors` directly.
 */
@Immutable
data class ExtendedColors(
    val primaryHover: Color,
    val destructiveHover: Color,
    val destructiveSecondaryOutline: Color,
    val disabledOutline: Color,
    val disabledFill: Color,
    val successOutline: Color,
    val success: Color,
    val onSuccess: Color,
    val secondaryFill: Color,

    val textPrimary: Color,
    val textTertiary: Color,
    val textSecondary: Color,
    val textPlaceholder: Color,
    val textDisabled: Color,

    val surfaceLower: Color,
    val surfaceHigher: Color,
    val surfaceOutline: Color,
    val overlay: Color,

    val yellowCardBackground: Color,
    val yellowCardText: Color,
    val redCardBackground: Color,
    val redCardText: Color,
)

private fun TriangulacionColors.toExtended() = ExtendedColors(
    primaryHover = brand,
    destructiveHover = danger,
    destructiveSecondaryOutline = danger,
    disabledOutline = line,
    disabledFill = surface300,
    successOutline = accentText,
    success = accentText,
    onSuccess = onBrand,
    secondaryFill = surface300,

    textPrimary = ink,
    textTertiary = inkMuted,
    textSecondary = inkMuted,
    textPlaceholder = inkSubtle,
    textDisabled = inkSubtle,

    surfaceLower = surface100,
    surfaceHigher = surface300,
    surfaceOutline = line,
    overlay = scrim,

    yellowCardBackground = cardYellow,
    yellowCardText = onVolt,
    redCardBackground = cardRed,
    // White reaches AA on the Tiza red; on the lighter Noche red the dark ink does
    redCardText = if (isDark) onVolt else Color.White,
)

val LightExtendedColors = TizaColors.toExtended()
val DarkExtendedColors = NocheColors.toExtended()

/** Material 3 roles from the tokens: screens on surface-100, cards and sheets on surface-200, sunken on surface-300. */
private fun TriangulacionColors.toColorScheme(): ColorScheme {
    val scheme = if (isDark) darkColorScheme() else lightColorScheme()
    return scheme.copy(
        primary = brand,
        onPrimary = onBrand,
        primaryContainer = successSoft,
        onPrimaryContainer = accentText,
        inversePrimary = volt,

        secondary = volt,
        onSecondary = onVolt,
        secondaryContainer = surface300,
        onSecondaryContainer = ink,

        tertiary = accentText,
        onTertiary = surface100,
        tertiaryContainer = successSoft,
        onTertiaryContainer = accentText,

        error = danger,
        onError = surface100,
        errorContainer = dangerSoft,
        onErrorContainer = danger,

        background = surface100,
        onBackground = ink,
        surface = surface200,
        onSurface = ink,
        surfaceVariant = surface300,
        onSurfaceVariant = inkMuted,
        surfaceTint = Color.Transparent,
        surfaceBright = surface200,
        surfaceDim = surface300,
        surfaceContainerLowest = surface200,
        surfaceContainerLow = surface200,
        surfaceContainer = surface200,
        surfaceContainerHigh = surface200,
        surfaceContainerHighest = surface300,
        inverseSurface = surfaceInverse,
        inverseOnSurface = onInverse,

        outline = lineStrong,
        outlineVariant = line,
        scrim = scrim,
    )
}

val LightColorScheme = TizaColors.toColorScheme()
val DarkColorScheme = NocheColors.toColorScheme()
