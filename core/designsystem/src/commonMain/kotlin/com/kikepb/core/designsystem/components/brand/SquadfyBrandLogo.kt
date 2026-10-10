package com.kikepb.core.designsystem.components.brand

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kikepb.core.designsystem.triangulacion.theme.Triangulacion
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import squadfy_app.core.designsystem.generated.resources.Res
import squadfy_app.core.designsystem.generated.resources.app_name
import squadfy_app.core.designsystem.generated.resources.squadfy_mark
import squadfy_app.core.designsystem.generated.resources.squadfy_mark_inverse
import squadfy_app.core.designsystem.generated.resources.squadfy_wordmark

/**
 * The Corte mark (spec 018): ink container on light backgrounds, the inverse (Volt container) on dark ones. By
 * default it follows the theme; a dark block in the light theme (welcome, ticket) passes [onDarkBackground].
 */
@Composable
fun SquadfyBrandLogo(
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    onDarkBackground: Boolean = Triangulacion.colors.isDark
) {
    Image(
        painter = painterResource(if (onDarkBackground) Res.drawable.squadfy_mark_inverse else Res.drawable.squadfy_mark),
        contentDescription = null,
        modifier = modifier.size(size)
    )
}

/** Mark plus the lowercase wordmark, the lockup of the welcome and login screens. */
@Composable
fun SquadfyBrandLockup(
    modifier: Modifier = Modifier,
    markSize: Dp = 40.dp,
    onDarkBackground: Boolean = Triangulacion.colors.isDark
) {
    val colors = Triangulacion.colors
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(markSize * 0.3f)
    ) {
        SquadfyBrandLogo(size = markSize, onDarkBackground = onDarkBackground)
        Image(
            painter = painterResource(Res.drawable.squadfy_wordmark),
            contentDescription = stringResource(Res.string.app_name),
            colorFilter = ColorFilter.tint(if (onDarkBackground) colors.onInverse else colors.ink),
            modifier = Modifier.height(markSize * 0.82f)
        )
    }
}
