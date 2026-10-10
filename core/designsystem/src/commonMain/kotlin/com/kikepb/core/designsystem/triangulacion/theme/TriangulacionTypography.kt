package com.kikepb.core.designsystem.triangulacion.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

/**
 * Type scale of `tokens.json` (spec 018). Display and figures in Bricolage Grotesque, text and UI in Instrument Sans.
 * Figures that change (score, rating, places, countdown) use [tabular] so they do not jump.
 */
@Immutable
class TriangulacionTypography(display: FontFamily, sans: FontFamily) {
    val statXl = TextStyle(fontFamily = display, fontWeight = FontWeight.ExtraBold, fontSize = 56.sp, lineHeight = 52.sp, letterSpacing = (-0.02).em, fontFeatureSettings = TABULAR)
    val displayXl = TextStyle(fontFamily = display, fontWeight = FontWeight.Bold, fontSize = 40.sp, lineHeight = 42.sp, letterSpacing = (-0.02).em)
    val displayLg = TextStyle(fontFamily = display, fontWeight = FontWeight.Bold, fontSize = 30.sp, lineHeight = 34.sp, letterSpacing = (-0.015).em)
    val displayMd = TextStyle(fontFamily = display, fontWeight = FontWeight.SemiBold, fontSize = 24.sp, lineHeight = 28.sp, letterSpacing = (-0.01).em)
    val title = TextStyle(fontFamily = display, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, lineHeight = 24.sp)
    val bodyLg = TextStyle(fontFamily = sans, fontWeight = FontWeight.Normal, fontSize = 17.sp, lineHeight = 26.sp)
    val body = TextStyle(fontFamily = sans, fontWeight = FontWeight.Normal, fontSize = 15.sp, lineHeight = 22.sp)
    val bodySm = TextStyle(fontFamily = sans, fontWeight = FontWeight.Normal, fontSize = 13.sp, lineHeight = 18.sp)
    val label = TextStyle(fontFamily = sans, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 18.sp)
    val labelCaps = TextStyle(fontFamily = sans, fontWeight = FontWeight.Bold, fontSize = 11.sp, lineHeight = 14.sp, letterSpacing = 0.08.em)

    companion object {
        const val TABULAR = "tnum"
    }
}

/** Tabular digits for any style. */
val TextStyle.tabular: TextStyle get() = copy(fontFeatureSettings = TriangulacionTypography.TABULAR)

val LocalTriangulacionTypography = staticCompositionLocalOf<TriangulacionTypography> {
    error("Wrap the UI in SquadfyTheme")
}
