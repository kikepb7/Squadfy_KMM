package com.kikepb.core.designsystem.components.brand

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import squadfy_app.core.designsystem.generated.resources.Res
import squadfy_app.core.designsystem.generated.resources.squadfy_logo

/** The Squadfy app icon (spec 011, AC-011-06). Callers can set a smaller size; by default it is at most 40 dp. */
@Composable
fun SquadfyBrandLogo(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(Res.drawable.squadfy_logo),
        contentDescription = null,
        modifier = modifier.sizeIn(maxWidth = 40.dp, maxHeight = 40.dp)
    )
}
