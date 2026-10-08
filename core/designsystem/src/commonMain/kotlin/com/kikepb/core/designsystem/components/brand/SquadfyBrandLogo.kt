package com.kikepb.core.designsystem.components.brand

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import squadfy_app.core.designsystem.generated.resources.Res
import squadfy_app.core.designsystem.generated.resources.squadfy_isotipo
import squadfy_app.core.designsystem.generated.resources.squadfy_isotipo_reverse

/**
 * The Squadfy isotype (brand kit, spec 016): the "color" version on light backgrounds and the "reverse" one on
 * dark ones, as the kit prescribes. By default it follows `surface`; screens that draw it on `background` (the
 * auth header, dark in both themes) pass [onDarkBackground]. Callers can shrink it; by default it is at most 40 dp tall.
 */
@Composable
fun SquadfyBrandLogo(
    modifier: Modifier = Modifier,
    onDarkBackground: Boolean = MaterialTheme.colorScheme.surface.luminance() < 0.5f
) {
    Image(
        painter = painterResource(if (onDarkBackground) Res.drawable.squadfy_isotipo_reverse else Res.drawable.squadfy_isotipo),
        contentDescription = null,
        modifier = modifier.sizeIn(maxWidth = 38.dp, maxHeight = 40.dp)
    )
}
