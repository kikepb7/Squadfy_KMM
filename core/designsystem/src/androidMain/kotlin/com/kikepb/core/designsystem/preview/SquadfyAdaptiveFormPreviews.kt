package com.kikepb.core.designsystem.preview

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import com.kikepb.core.designsystem.components.brand.SquadfyBrandLogo
import com.kikepb.core.designsystem.components.layouts.SquadfyAdaptiveFormLayout
import com.kikepb.core.designsystem.theme.SquadfyTheme

@Composable
@PreviewLightDark
@PreviewScreenSizes
fun SquadfyAdaptiveFormLayoutLightPreview() {
    SquadfyTheme {
        SquadfyAdaptiveFormLayout(
            headerText = "Welcome to Squadfy!",
            errorText = "Login failed!",
            logo = { SquadfyBrandLogo() },
            formContent = {
                Text(
                    text = "Sample form title",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Sample form title 2",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        )
    }
}