package org.kikepb.squadfy.debug

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kikepb.core.designsystem.components.buttons.SquadfyButton
import com.kikepb.core.designsystem.components.buttons.SquadfyButtonStyle
import com.kikepb.core.designsystem.components.topbar.SquadfyTopBar
import com.kikepb.core.domain.featureflag.FlagValueSource
import com.kikepb.core.domain.featureflag.ResolvedFeatureFlag
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import squadfy_app.composeapp.generated.resources.Res
import squadfy_app.composeapp.generated.resources.feature_flags_environment
import squadfy_app.composeapp.generated.resources.feature_flags_pre_only_hint
import squadfy_app.composeapp.generated.resources.feature_flags_reset
import squadfy_app.composeapp.generated.resources.feature_flags_source_default
import squadfy_app.composeapp.generated.resources.feature_flags_source_override
import squadfy_app.composeapp.generated.resources.feature_flags_source_remote
import squadfy_app.composeapp.generated.resources.feature_flags_title

@Composable
fun FeatureFlagsRoot(
    onBackClick: () -> Unit,
    viewModel: FeatureFlagsViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    FeatureFlagsScreen(
        state = state,
        onAction = { action ->
            if (action is FeatureFlagsAction.OnBackClick) onBackClick() else viewModel.onAction(action)
        }
    )
}

@Composable
fun FeatureFlagsScreen(
    state: FeatureFlagsState,
    onAction: (FeatureFlagsAction) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().navigationBarsPadding()) {
        SquadfyTopBar(
            title = stringResource(Res.string.feature_flags_title),
            onBackClick = { onAction(FeatureFlagsAction.OnBackClick) }
        )
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)
        ) {
            item {
                Text(
                    text = stringResource(Res.string.feature_flags_environment, state.environment.name),
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = stringResource(Res.string.feature_flags_pre_only_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            items(items = state.flags, key = { it.flag.key }) { resolved ->
                FeatureFlagRow(
                    resolved = resolved,
                    onToggle = { enabled -> onAction(FeatureFlagsAction.OnToggle(flag = resolved.flag, enabled = enabled)) }
                )
            }
            item {
                SquadfyButton(
                    text = stringResource(Res.string.feature_flags_reset),
                    onClick = { onAction(FeatureFlagsAction.OnReset) },
                    style = SquadfyButtonStyle.SECONDARY,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun FeatureFlagRow(
    resolved: ResolvedFeatureFlag,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(text = resolved.flag.key, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = resolved.flag.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(
                    when (resolved.source) {
                        FlagValueSource.DEFAULT -> Res.string.feature_flags_source_default
                        FlagValueSource.REMOTE -> Res.string.feature_flags_source_remote
                        FlagValueSource.OVERRIDE -> Res.string.feature_flags_source_override
                    }
                ),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Switch(checked = resolved.enabled, onCheckedChange = onToggle)
    }
}
