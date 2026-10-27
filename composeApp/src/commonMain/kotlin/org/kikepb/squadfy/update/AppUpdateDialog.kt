package org.kikepb.squadfy.update

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kikepb.core.designsystem.triangulacion.components.CutLg
import com.kikepb.core.designsystem.triangulacion.components.TriButton
import com.kikepb.core.designsystem.triangulacion.components.TriButtonVariant
import com.kikepb.core.designsystem.triangulacion.preview.ScreenPreview
import com.kikepb.core.designsystem.triangulacion.theme.Triangulacion
import com.kikepb.core.domain.update.UpdateStatus
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import squadfy_app.composeapp.generated.resources.Res
import squadfy_app.composeapp.generated.resources.update_later
import squadfy_app.composeapp.generated.resources.update_now
import squadfy_app.composeapp.generated.resources.update_optional_body
import squadfy_app.composeapp.generated.resources.update_optional_title
import squadfy_app.composeapp.generated.resources.update_required_body
import squadfy_app.composeapp.generated.resources.update_required_title

/** Spec 021 APP-RN-P8: the new-version notice over any screen. */
@Composable
fun AppUpdateRoot(viewModel: AppUpdateViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val uriHandler = LocalUriHandler.current
    AppUpdateDialog(
        state = state,
        onUpdateClick = {
            viewModel.onAction(AppUpdateAction.OnUpdateClick)
            if (state.storeUrl.isNotBlank()) runCatching { uriHandler.openUri(state.storeUrl) }
        },
        onLaterClick = { viewModel.onAction(AppUpdateAction.OnLaterClick) }
    )
}

@Composable
fun AppUpdateDialog(
    state: AppUpdateState,
    onUpdateClick: () -> Unit,
    onLaterClick: () -> Unit
) {
    if (state.status == UpdateStatus.UP_TO_DATE) return
    val required = state.status == UpdateStatus.REQUIRED
    Dialog(
        // A required update cannot be dismissed: back and a tap outside do nothing
        onDismissRequest = { if (!required) onLaterClick() },
        properties = DialogProperties(dismissOnBackPress = !required, dismissOnClickOutside = !required, usePlatformDefaultWidth = false)
    ) {
        AppUpdateContent(state = state, onUpdateClick = onUpdateClick, onLaterClick = onLaterClick)
    }
}

@Composable
private fun AppUpdateContent(
    state: AppUpdateState,
    onUpdateClick: () -> Unit,
    onLaterClick: () -> Unit
) {
    val colors = Triangulacion.colors
    val type = Triangulacion.type
    val required = state.status == UpdateStatus.REQUIRED
    Column(
        modifier = Modifier
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .widthIn(max = 480.dp)
            .background(color = colors.surface100, shape = CutLg)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = stringResource(if (required) Res.string.update_required_title else Res.string.update_optional_title),
            style = type.displayMd,
            color = colors.ink,
            modifier = Modifier.semantics { heading() }
        )
        Text(
            text = stringResource(if (required) Res.string.update_required_body else Res.string.update_optional_body, state.latestVersion),
            style = type.body,
            color = colors.inkMuted
        )
        TriButton(
            text = stringResource(Res.string.update_now),
            onClick = onUpdateClick,
            variant = TriButtonVariant.Volt,
            enabled = state.storeUrl.isNotBlank(),
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        )
        if (!required) {
            TriButton(
                text = stringResource(Res.string.update_later),
                onClick = onLaterClick,
                variant = TriButtonVariant.Secondary,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Preview
@Composable
private fun AppUpdateContentTizaPreview() {
    ScreenPreview {
        AppUpdateContent(state = AppUpdateState(UpdateStatus.OPTIONAL, latestVersion = "1.5.0", storeUrl = "https://"), onUpdateClick = {}, onLaterClick = {})
    }
}

@Preview
@Composable
private fun AppUpdateContentNochePreview() {
    ScreenPreview(darkTheme = true) {
        AppUpdateContent(state = AppUpdateState(UpdateStatus.REQUIRED, latestVersion = "1.5.0", storeUrl = "https://"), onUpdateClick = {}, onLaterClick = {})
    }
}
