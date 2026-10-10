package com.kikepb.core.designsystem.triangulacion.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.dp

/** Spacing grid of 4 dp (`space-*` in tokens.json). Screen gutter [s4], between sections [s6], touch target [s12]. */
@Immutable
object Space {
    val s1 = 4.dp
    val s2 = 8.dp
    val s3 = 12.dp
    val s4 = 16.dp
    val s5 = 20.dp
    val s6 = 24.dp
    val s8 = 32.dp
    val s12 = 48.dp
    val s16 = 64.dp
}

/** Corner radii and the Corte sizes (top-right corner bevelled at 45°). */
@Immutable
object Radius {
    val xs = 6.dp
    val sm = 10.dp
    val md = 14.dp
    val lg = 20.dp
    val xl = 28.dp
    val cutSm = 8.dp
    val cutMd = 14.dp
    val cutLg = 24.dp
}

@Immutable
object StrokeWidth {
    val hair = 1.dp
    val base = 1.5.dp
    val focus = 2.dp
}

/** Slant of the segments (places, balance, tabs): −18°. tan(18°), precomputed for common code. */
const val SKEW_TAN = 0.3249197f
