package com.kikepb.core.designsystem.theme

import androidx.compose.ui.graphics.Color

// Brand kit (spec 016, `05-tokens/tokens.json`). Token names are kept so every usage follows the new palette.
// Brand colors
val SquadfyBrand1000 = Color(0xFF0A2E22) // Night Pitch: main color, dark backgrounds, text on light
val SquadfyBrand900 = Color(0xFF13804F) // Squad Green: crest on dark, primary on light (4.97:1 with white), success
val SquadfyBrand600 = Color(0xFFB4E02A) // Peto Lime, pressed/hover
val SquadfyBrand500 = Color(0xFFC8F53C) // Peto Lime: accent and primary on dark. Never as text on light backgrounds
val SquadfyBrand500Alpha40 = Color(0x66C8F53C)
val SquadfyBrand100 = Color(0xFFE8F8C2) // Lime tint for light containers

// Base Colors: the kit's green-tinted neutrals (0, 50, 100, 200, 400, 600) plus Night Pitch tones for dark surfaces
val SquadfyBase1000 = Color(0xFF0A2E22) // Night Pitch
val SquadfyBase1000Alpha8 = Color(0x140A2E22)
val SquadfyBase1000Alpha80 = Color(0xCC0A2E22)
val SquadfyBase950 = Color(0xFF193B2F) // Night Pitch + 6% white: dark surface
val SquadfyBase900 = Color(0xFF2C4B41) // Night Pitch + 14% white: dark raised surface, light secondary text
val SquadfyBase800 = Color(0xFF3A5247)
val SquadfyBase700 = Color(0xFF4F6359) // kit neutral 600: light placeholder text (5.86:1 on Chalk)
val SquadfyBase500 = Color(0xFF6E8178)
val SquadfyBase400 = Color(0xFFA8B8AF) // dark placeholder text (AA on dark surfaces)
val SquadfyBase200 = Color(0xFFC9D3CC) // kit neutral 200
val SquadfyBase150 = Color(0xFFE4E9E2) // kit neutral 100
val SquadfyBase100 = Color(0xFFF3F5EF) // Chalk: light background, text on dark
val SquadfyBase100Alpha10 = Color(0x1AF3F5EF)
val SquadfyBase1000Alpha14 = Color(0x140A2E22) // 8% alpha for light mode surface outline
val SquadfyBase100Alpha10Alt = Color(0x1AF3F5EF) // 10% alpha for dark mode surface outline
val SquadfyBase0 = Color(0xFFFFFFFF)

// Dark theme surfaces: the original Squadfy blue-slate tones, kept at the owner's request (spec 016).
// Brand accents (lime, Squad Green) and the light theme follow the brand kit.
val SquadfyDark1000 = Color(0xFF101C28) // background, surfaceLower
val SquadfyDark1000Alpha80 = Color(0xCC101C28) // scrim
val SquadfyDark950 = Color(0xFF1C2A39) // surface
val SquadfyDark900 = Color(0xFF2F3F4F) // raised surface, containers
val SquadfyDark800 = Color(0xFF475767) // outline variant

// Red Colors (kit "red card" #D93636)
val SquadfyRed600 = Color(0xFFB02A2A)
val SquadfyRed500 = Color(0xFFCC3131) // kit red card #D93636 darkened for AA: 4.72:1 on Chalk, 5.18:1 with white content
val SquadfyRed200 = Color(0xFFF9A3A3) // error on dark: ≥4.93:1 on every dark surface, 7.57:1 with Night Pitch content

// Accent Colors (15% alpha)
val SquadfyBlue = Color(0x26A2C0FF)
val SquadfyPurple = Color(0x26CAADFF)
val SquadfyViolet = Color(0x26FCB6FF)
val SquadfyPink = Color(0x26FFA6AF)
val SquadfyOrange = Color(0x26FEC5A7)
val SquadfyYellow = Color(0x26FEF4A5)
val SquadfyGreen = Color(0x26C2FFA6)
val SquadfyTeal = Color(0x26A0FFE0)
val SquadfyLightBlue = Color(0x2698E4FF)
val SquadfyGrey = Color(0x26D0D7DD)

// Cake Colors - Light Theme (Soft pastel shades with good contrast)
val SquadfyCakeLightViolet = Color(0xFFF0D9FF) // Soft violet
val SquadfyCakeLightGreen = Color(0xFFDEFFD9) // Soft green
val SquadfyCakeLightBlue = Color(0xFFD9E8FF) // Soft blue
val SquadfyCakeLightPink = Color(0xFFFFE0E6) // Soft pink
val SquadfyCakeLightOrange = Color(0xFFFFE8D9) // Soft orange
val SquadfyCakeLightYellow = Color(0xFFFFF9D9) // Soft yellow
val SquadfyCakeLightTeal = Color(0xFFD9FFF2) // Soft teal
val SquadfyCakeLightPurple = Color(0xFFE6D9FF) // Soft purple
val SquadfyCakeLightRed = Color(0xFFFFD9DC) // Soft red
val SquadfyCakeLightMint = Color(0xFFE0FFE8) // Soft mint

// Disciplinary card colors — Light
val SquadfyYellowCardBgLight = Color(0xFFFEF4A5)
val SquadfyYellowCardTextLight = Color(0xFF7A6000)
val SquadfyRedCardBgLight = Color(0xFFFFD9DC)
val SquadfyRedCardTextLight = Color(0xFF8B0000)

// Disciplinary card colors — Dark
val SquadfyYellowCardBgDark = Color(0x26FEF4A5)
val SquadfyYellowCardTextDark = Color(0xFFFEF0A0)
val SquadfyRedCardBgDark = Color(0x26FF8A95)
val SquadfyRedCardTextDark = Color(0xFFFFAAB5)

// Cake Colors - Dark Theme (Muted shades with good contrast)
val SquadfyCakeDarkViolet = Color(0x26FCB6FF) // Muted violet (15% alpha)
val SquadfyCakeDarkGreen = Color(0x26C2FFA6) // Muted green (15% alpha)
val SquadfyCakeDarkBlue = Color(0x26A2C0FF) // Muted blue (15% alpha)
val SquadfyCakeDarkPink = Color(0x26FFA6AF) // Muted pink (15% alpha)
val SquadfyCakeDarkOrange = Color(0x26FEC5A7) // Muted orange (15% alpha)
val SquadfyCakeDarkYellow = Color(0x26FEF4A5) // Muted yellow (15% alpha)
val SquadfyCakeDarkTeal = Color(0x26A0FFE0) // Muted teal (15% alpha)
val SquadfyCakeDarkPurple = Color(0x26CAADFF) // Muted purple (15% alpha)
val SquadfyCakeDarkRed = Color(0x26FF8A95) // Muted red (15% alpha)
val SquadfyCakeDarkMint = Color(0x26A6FFCC) // Muted mint (15% alpha)