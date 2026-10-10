package com.kikepb.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.kikepb.core.designsystem.triangulacion.theme.Triangulacion
import com.kikepb.core.designsystem.triangulacion.theme.TriangulacionTypography
import org.jetbrains.compose.resources.Font
import squadfy_app.core.designsystem.generated.resources.Res
import squadfy_app.core.designsystem.generated.resources.bricolagegrotesque_bold
import squadfy_app.core.designsystem.generated.resources.bricolagegrotesque_extrabold
import squadfy_app.core.designsystem.generated.resources.bricolagegrotesque_medium
import squadfy_app.core.designsystem.generated.resources.bricolagegrotesque_semibold
import squadfy_app.core.designsystem.generated.resources.instrumentsans_bold
import squadfy_app.core.designsystem.generated.resources.instrumentsans_medium
import squadfy_app.core.designsystem.generated.resources.instrumentsans_regular
import squadfy_app.core.designsystem.generated.resources.instrumentsans_semibold

/** Display and figures (spec 018). Bricolage Grotesque, SIL Open Font License 1.1 (see FONTS.md). */
val BricolageGrotesque @Composable get() = FontFamily(
    Font(resource = Res.font.bricolagegrotesque_medium, weight = FontWeight.Medium),
    Font(resource = Res.font.bricolagegrotesque_semibold, weight = FontWeight.SemiBold),
    Font(resource = Res.font.bricolagegrotesque_bold, weight = FontWeight.Bold),
    Font(resource = Res.font.bricolagegrotesque_extrabold, weight = FontWeight.ExtraBold),
)

/** Text and UI (spec 018). Instrument Sans, SIL Open Font License 1.1 (see FONTS.md). */
val InstrumentSans @Composable get() = FontFamily(
    Font(resource = Res.font.instrumentsans_regular, weight = FontWeight.Normal),
    Font(resource = Res.font.instrumentsans_medium, weight = FontWeight.Medium),
    Font(resource = Res.font.instrumentsans_semibold, weight = FontWeight.SemiBold),
    Font(resource = Res.font.instrumentsans_bold, weight = FontWeight.Bold),
)

val Typography.labelXSmall: TextStyle
    @Composable get() = Triangulacion.type.labelCaps

val Typography.titleXSmall: TextStyle
    @Composable get() = Triangulacion.type.title.copy(fontSize = 15.sp, lineHeight = 20.sp)

/** Material 3 roles on the Triangulación scale, for the components that still read `MaterialTheme.typography`. */
fun squadfyTypography(t: TriangulacionTypography) = Typography(
    displayLarge = t.statXl,
    displayMedium = t.displayXl,
    displaySmall = t.labelCaps,
    headlineLarge = t.displayLg,
    headlineMedium = t.displayMd,
    headlineSmall = t.label,
    titleLarge = t.displayLg,
    titleMedium = t.displayMd,
    titleSmall = t.title,
    bodyLarge = t.bodyLg,
    bodyMedium = t.body,
    bodySmall = t.bodySm,
    labelLarge = t.label.copy(fontSize = 15.sp, lineHeight = 20.sp),
    labelMedium = t.label.copy(fontSize = 15.sp, lineHeight = 20.sp),
    labelSmall = t.label,
)
