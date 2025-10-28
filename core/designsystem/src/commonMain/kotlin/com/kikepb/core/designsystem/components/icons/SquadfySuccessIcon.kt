package com.kikepb.core.designsystem.components.icons

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.kikepb.core.designsystem.theme.extended
import org.jetbrains.compose.resources.vectorResource
import squadfy_app.core.designsystem.generated.resources.Res
import squadfy_app.core.designsystem.generated.resources.success_checkmark

@Composable
fun SquadfySuccessIcon(modifier: Modifier = Modifier) {
    Icon(
        imageVector = vectorResource(Res.drawable.success_checkmark),
        contentDescription = null,
        tint = MaterialTheme.colorScheme.extended.success,
        modifier = modifier
    )
}