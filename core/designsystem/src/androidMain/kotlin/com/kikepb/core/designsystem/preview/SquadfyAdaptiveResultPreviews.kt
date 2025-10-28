package com.kikepb.core.designsystem.preview

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import com.kikepb.core.designsystem.components.layouts.SquadfyAdaptiveResultLayout
import com.kikepb.core.designsystem.theme.SquadfyTheme

@Composable
@PreviewLightDark
@PreviewScreenSizes
fun SquadfyAdaptiveResultLayoutPreview() {
    SquadfyTheme {
        SquadfyAdaptiveResultLayout(
            modifier = Modifier.fillMaxSize(),
            content = {
                Text(
                    text = "Registration successful!",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        )
    }
}