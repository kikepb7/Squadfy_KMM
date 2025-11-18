package com.kikepb.core.designsystem.components.dialogs

import androidx.compose.runtime.Composable
import com.kikepb.core.presentation.util.currentDeviceConfiguration

@Composable
fun SquadfyAdaptiveDialogSheetLayout(
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    val configuration = currentDeviceConfiguration()

    if (configuration.isMobile) {
        SquadfyBottomSheet(
            onDismiss = onDismiss,
            content = content
        )
    } else {
        SquadfyDialogContent(
            onDismiss = onDismiss,
            content = content
        )
    }
}