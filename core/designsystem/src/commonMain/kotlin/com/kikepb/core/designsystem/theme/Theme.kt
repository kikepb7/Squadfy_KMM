package com.kikepb.core.designsystem.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val LocalExtendedColors = staticCompositionLocalOf { LightExtendedColors }

val ColorScheme.extended: ExtendedColors
    @ReadOnlyComposable
    @Composable
    get() = LocalExtendedColors.current

@Immutable
data class ExtendedColors(
    // Button states
    val primaryHover: Color,
    val destructiveHover: Color,
    val destructiveSecondaryOutline: Color,
    val disabledOutline: Color,
    val disabledFill: Color,
    val successOutline: Color,
    val success: Color,
    val onSuccess: Color,
    val secondaryFill: Color,

    // Text variants
    val textPrimary: Color,
    val textTertiary: Color,
    val textSecondary: Color,
    val textPlaceholder: Color,
    val textDisabled: Color,

    // Surface variants
    val surfaceLower: Color,
    val surfaceHigher: Color,
    val surfaceOutline: Color,
    val overlay: Color,

    // Accent colors
    val accentBlue: Color,
    val accentPurple: Color,
    val accentViolet: Color,
    val accentPink: Color,
    val accentOrange: Color,
    val accentYellow: Color,
    val accentGreen: Color,
    val accentTeal: Color,
    val accentLightBlue: Color,
    val accentGrey: Color,

    // Cake colors for chat bubbles
    val cakeViolet: Color,
    val cakeGreen: Color,
    val cakeBlue: Color,
    val cakePink: Color,
    val cakeOrange: Color,
    val cakeYellow: Color,
    val cakeTeal: Color,
    val cakePurple: Color,
    val cakeRed: Color,
    val cakeMint: Color,
)

val LightExtendedColors = ExtendedColors(
    primaryHover = SquadfyBrand600,
    destructiveHover = SquadfyRed600,
    destructiveSecondaryOutline = SquadfyRed200,
    disabledOutline = SquadfyBase200,
    disabledFill = SquadfyBase150,
    successOutline = SquadfyBrand100,
    success = SquadfyBrand600,
    onSuccess = SquadfyBase0,
    secondaryFill = SquadfyBase100,

    textPrimary = SquadfyBase1000,
    textTertiary = SquadfyBase800,
    textSecondary = SquadfyBase900,
    textPlaceholder = SquadfyBase700,
    textDisabled = SquadfyBase400,

    surfaceLower = SquadfyBase100,
    surfaceHigher = SquadfyBase100,
    surfaceOutline = SquadfyBase1000Alpha14,
    overlay = SquadfyBase1000Alpha80,

    accentBlue = SquadfyBlue,
    accentPurple = SquadfyPurple,
    accentViolet = SquadfyViolet,
    accentPink = SquadfyPink,
    accentOrange = SquadfyOrange,
    accentYellow = SquadfyYellow,
    accentGreen = SquadfyGreen,
    accentTeal = SquadfyTeal,
    accentLightBlue = SquadfyLightBlue,
    accentGrey = SquadfyGrey,

    cakeViolet = SquadfyCakeLightViolet,
    cakeGreen = SquadfyCakeLightGreen,
    cakeBlue = SquadfyCakeLightBlue,
    cakePink = SquadfyCakeLightPink,
    cakeOrange = SquadfyCakeLightOrange,
    cakeYellow = SquadfyCakeLightYellow,
    cakeTeal = SquadfyCakeLightTeal,
    cakePurple = SquadfyCakeLightPurple,
    cakeRed = SquadfyCakeLightRed,
    cakeMint = SquadfyCakeLightMint,
)

val DarkExtendedColors = ExtendedColors(
    primaryHover = SquadfyBrand600,
    destructiveHover = SquadfyRed600,
    destructiveSecondaryOutline = SquadfyRed200,
    disabledOutline = SquadfyBase900,
    disabledFill = SquadfyBase1000,
    successOutline = SquadfyBrand500Alpha40,
    success = SquadfyBrand500,
    onSuccess = SquadfyBase1000,
    secondaryFill = SquadfyBase900,

    textPrimary = SquadfyBase0,
    textTertiary = SquadfyBase200,
    textSecondary = SquadfyBase150,
    textPlaceholder = SquadfyBase400,
    textDisabled = SquadfyBase500,

    surfaceLower = SquadfyBase1000,
    surfaceHigher = SquadfyBase900,
    surfaceOutline = SquadfyBase100Alpha10Alt,
    overlay = SquadfyBase1000Alpha80,

    accentBlue = SquadfyBlue,
    accentPurple = SquadfyPurple,
    accentViolet = SquadfyViolet,
    accentPink = SquadfyPink,
    accentOrange = SquadfyOrange,
    accentYellow = SquadfyYellow,
    accentGreen = SquadfyGreen,
    accentTeal = SquadfyTeal,
    accentLightBlue = SquadfyLightBlue,
    accentGrey = SquadfyGrey,

    cakeViolet = SquadfyCakeDarkViolet,
    cakeGreen = SquadfyCakeDarkGreen,
    cakeBlue = SquadfyCakeDarkBlue,
    cakePink = SquadfyCakeDarkPink,
    cakeOrange = SquadfyCakeDarkOrange,
    cakeYellow = SquadfyCakeDarkYellow,
    cakeTeal = SquadfyCakeDarkTeal,
    cakePurple = SquadfyCakeDarkPurple,
    cakeRed = SquadfyCakeDarkRed,
    cakeMint = SquadfyCakeDarkMint,
)

val LightColorScheme = lightColorScheme(
    primary = SquadfyBrand500,
    onPrimary = SquadfyBrand1000,
    primaryContainer = SquadfyBrand100,
    onPrimaryContainer = SquadfyBrand900,

    secondary = SquadfyBase700,
    onSecondary = SquadfyBase0,
    secondaryContainer = SquadfyBase100,
    onSecondaryContainer = SquadfyBase900,

    tertiary = SquadfyBrand900,
    onTertiary = SquadfyBase0,
    tertiaryContainer = SquadfyBrand100,
    onTertiaryContainer = SquadfyBrand1000,

    error = SquadfyRed500,
    onError = SquadfyBase0,
    errorContainer = SquadfyRed200,
    onErrorContainer = SquadfyRed600,

    background = SquadfyBrand1000,
    onBackground = SquadfyBase0,
    surface = SquadfyBase0,
    onSurface = SquadfyBase1000,
    surfaceVariant = SquadfyBase100,
    onSurfaceVariant = SquadfyBase900,

    outline = SquadfyBase1000Alpha8,
    outlineVariant = SquadfyBase200,
)

val DarkColorScheme = darkColorScheme(
    primary = SquadfyBrand500,
    onPrimary = SquadfyBrand1000,
    primaryContainer = SquadfyBrand900,
    onPrimaryContainer = SquadfyBrand500,

    secondary = SquadfyBase400,
    onSecondary = SquadfyBase1000,
    secondaryContainer = SquadfyBase900,
    onSecondaryContainer = SquadfyBase150,

    tertiary = SquadfyBrand500,
    onTertiary = SquadfyBase1000,
    tertiaryContainer = SquadfyBrand900,
    onTertiaryContainer = SquadfyBrand500,

    error = SquadfyRed500,
    onError = SquadfyBase0,
    errorContainer = SquadfyRed600,
    onErrorContainer = SquadfyRed200,

    background = SquadfyBase1000,
    onBackground = SquadfyBase0,
    surface = SquadfyBase950,
    onSurface = SquadfyBase0,
    surfaceVariant = SquadfyBase900,
    onSurfaceVariant = SquadfyBase150,

    outline = SquadfyBase100Alpha10,
    outlineVariant = SquadfyBase800,
)