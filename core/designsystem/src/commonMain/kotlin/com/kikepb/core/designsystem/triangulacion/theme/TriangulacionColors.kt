package com.kikepb.core.designsystem.triangulacion.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/** Colour roles of Triangulación: Tiza (light) and Noche (dark). Names follow tokens.json (surface-100 → surface100). */
@Immutable
data class TriangulacionColors(
    val surface100: Color, val surface200: Color, val surface300: Color,
    val surfaceInverse: Color, val onInverse: Color, val onInverseMuted: Color,
    val ink: Color, val inkMuted: Color, val inkSubtle: Color,
    val line: Color, val lineStrong: Color,
    val brand: Color, val onBrand: Color,
    val volt: Color, val onVolt: Color, val accentText: Color,
    val teamA: Color, val onTeamA: Color, val teamB: Color, val onTeamB: Color,
    val teamAText: Color, val teamBText: Color,
    val successSoft: Color, val warning: Color, val warningSoft: Color,
    val danger: Color, val dangerSoft: Color,
    val cardYellow: Color, val cardRed: Color,
    val focusRing: Color, val scrim: Color,
    val isDark: Boolean,
)

val TizaColors = TriangulacionColors(
    surface100 = Color(0xFFF6F5EE), surface200 = Color(0xFFFFFFFF), surface300 = Color(0xFFEBE9DC),
    surfaceInverse = Color(0xFF0E1A12), onInverse = Color(0xFFF1F2EA), onInverseMuted = Color(0xFFB3BFB5),
    ink = Color(0xFF101A14), inkMuted = Color(0xFF4A5750), inkSubtle = Color(0xFF5F6C64),
    line = Color(0xFFDCD9CA), lineStrong = Color(0xFF7D8A81),
    brand = Color(0xFF0F4D36), onBrand = Color(0xFFF6F5EE),
    volt = Color(0xFFC5F65A), onVolt = Color(0xFF0E1A12), accentText = Color(0xFF17623F),
    teamA = Color(0xFFC5F65A), onTeamA = Color(0xFF0E1A12), teamB = Color(0xFFFF8A63), onTeamB = Color(0xFF1C0D07),
    teamAText = Color(0xFF17623F), teamBText = Color(0xFFA8381A),
    successSoft = Color(0xFFDFF5C8), warning = Color(0xFF7A5200), warningSoft = Color(0xFFFBEFC9),
    danger = Color(0xFFB3261E), dangerSoft = Color(0xFFFBE0DC),
    cardYellow = Color(0xFFF2C230), cardRed = Color(0xFFD63A2F),
    focusRing = Color(0xFF0F4D36), scrim = Color(0x80101A14), isDark = false,
)

val NocheColors = TriangulacionColors(
    surface100 = Color(0xFF0B120E), surface200 = Color(0xFF121C16), surface300 = Color(0xFF1A261F),
    surfaceInverse = Color(0xFF1F2F25), onInverse = Color(0xFFF1F2EA), onInverseMuted = Color(0xFFB3BFB5),
    ink = Color(0xFFF1F2EA), inkMuted = Color(0xFFA9B4AB), inkSubtle = Color(0xFF8D998F),
    line = Color(0xFF25332B), lineStrong = Color(0xFF66756B),
    brand = Color(0xFFC5F65A), onBrand = Color(0xFF0B120E),
    volt = Color(0xFFC5F65A), onVolt = Color(0xFF0E1A12), accentText = Color(0xFFC5F65A),
    teamA = Color(0xFFC5F65A), onTeamA = Color(0xFF0E1A12), teamB = Color(0xFFFF8A63), onTeamB = Color(0xFF1C0D07),
    teamAText = Color(0xFFC5F65A), teamBText = Color(0xFFFF9D7B),
    successSoft = Color(0xFF22351F), warning = Color(0xFFF3C15A), warningSoft = Color(0xFF3A2F12),
    danger = Color(0xFFFF8F86), dangerSoft = Color(0xFF3D1C19),
    cardYellow = Color(0xFFF2C230), cardRed = Color(0xFFEF5A4F),
    focusRing = Color(0xFFC5F65A), scrim = Color(0xA6000000), isDark = true,
)

val LocalTriangulacionColors = staticCompositionLocalOf { TizaColors }
