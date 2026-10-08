package com.kikepb.club.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kikepb.core.designsystem.components.buttons.SquadfyButton
import com.kikepb.core.designsystem.components.buttons.SquadfyButtonStyle
import com.kikepb.core.designsystem.theme.extended
import org.jetbrains.compose.resources.stringResource
import squadfy_app.feature.club.presentation.generated.resources.Res
import squadfy_app.feature.club.presentation.generated.resources.club_detail_load_error_hint
import squadfy_app.feature.club.presentation.generated.resources.common_load_error
import squadfy_app.feature.club.presentation.generated.resources.common_retry

/** Spec 017: the first load failed and there is nothing cached to show, so the tab offers a retry instead of a blank. */
@Composable
fun LoadErrorCard(onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth().padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(Res.string.common_load_error),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.extended.textPrimary
        )
        Text(
            text = stringResource(Res.string.club_detail_load_error_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.extended.textSecondary,
            textAlign = TextAlign.Center
        )
        SquadfyButton(text = stringResource(Res.string.common_retry), onClick = onRetry, style = SquadfyButtonStyle.SECONDARY)
    }
}
