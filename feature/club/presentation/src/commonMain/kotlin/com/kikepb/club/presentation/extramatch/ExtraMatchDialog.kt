package com.kikepb.club.presentation.extramatch

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kikepb.club.presentation.components.DatePickerDialog
import com.kikepb.club.presentation.components.TimePickerDialog
import com.kikepb.club.presentation.mapper.formatted
import com.kikepb.core.designsystem.components.textfields.SquadfyTextField
import com.kikepb.core.presentation.util.ObserveAsEvents
import kotlinx.datetime.LocalTime
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import squadfy_app.feature.club.presentation.generated.resources.Res
import squadfy_app.feature.club.presentation.generated.resources.common_cancel
import squadfy_app.feature.club.presentation.generated.resources.common_save
import squadfy_app.feature.club.presentation.generated.resources.extra_match
import squadfy_app.feature.club.presentation.generated.resources.extra_match_date
import squadfy_app.feature.club.presentation.generated.resources.extra_match_duration
import squadfy_app.feature.club.presentation.generated.resources.extra_match_pick
import squadfy_app.feature.club.presentation.generated.resources.extra_match_quick_test
import squadfy_app.feature.club.presentation.generated.resources.extra_match_time
import squadfy_app.feature.club.presentation.generated.resources.extra_match_title
import kotlin.time.Clock

/** "Extra match" button plus its form; [onCreated] refreshes the announcement and shows [message]. */
@Composable
fun ExtraMatchButton(
    onCreated: (message: com.kikepb.core.presentation.util.UiText) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ExtraMatchViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is ExtraMatchEvent.Created -> onCreated(event.message)
        }
    }
    OutlinedButton(onClick = { viewModel.onAction(ExtraMatchAction.OnOpen) }, modifier = modifier.fillMaxWidth()) {
        Text(text = stringResource(Res.string.extra_match))
    }
    if (state.isOpen) ExtraMatchForm(state = state, onAction = viewModel::onAction)
}

@Composable
private fun ExtraMatchForm(state: ExtraMatchState, onAction: (ExtraMatchAction) -> Unit) {
    when (state.picker) {
        ExtraMatchPicker.DATE -> {
            val today = Clock.System.now().toLocalDateTime(state.zone).date
            DatePickerDialog(
                isSelectable = { it >= today },
                onConfirm = { onAction(ExtraMatchAction.OnDatePicked(it)) },
                onDismiss = { onAction(ExtraMatchAction.OnDismissPicker) }
            )
            return
        }
        ExtraMatchPicker.TIME -> {
            TimePickerDialog(
                initial = state.time ?: LocalTime(20, 0),
                onConfirm = { onAction(ExtraMatchAction.OnTimePicked(it)) },
                onDismiss = { onAction(ExtraMatchAction.OnDismissPicker) }
            )
            return
        }
        null -> Unit
    }
    AlertDialog(
        onDismissRequest = { onAction(ExtraMatchAction.OnDismiss) },
        title = { Text(text = stringResource(Res.string.extra_match_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                PickRow(
                    text = stringResource(Res.string.extra_match_date, state.date?.let { "${it.day.toString().padStart(2, '0')}/${(it.month.ordinal + 1).toString().padStart(2, '0')}/${it.year}" } ?: "—"),
                    onPick = { onAction(ExtraMatchAction.OnPickDate) }
                )
                PickRow(
                    text = stringResource(Res.string.extra_match_time, state.time?.formatted() ?: "—") + " (${state.zone.id})",
                    onPick = { onAction(ExtraMatchAction.OnPickTime) }
                )
                SquadfyTextField(state = state.duration, title = stringResource(Res.string.extra_match_duration), singleLine = true)
                if (state.quickTestEnabled) {
                    TextButton(onClick = { onAction(ExtraMatchAction.OnQuickTest) }) { Text(text = stringResource(Res.string.extra_match_quick_test)) }
                }
                state.error?.let { Text(text = it.asString(), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
            }
        },
        confirmButton = {
            TextButton(onClick = { onAction(ExtraMatchAction.OnConfirm) }, enabled = state.canConfirm) { Text(text = stringResource(Res.string.common_save)) }
        },
        dismissButton = { TextButton(onClick = { onAction(ExtraMatchAction.OnDismiss) }) { Text(text = stringResource(Res.string.common_cancel)) } }
    )
}

@Composable
private fun PickRow(text: String, onPick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = text, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
        TextButton(onClick = onPick) { Text(text = stringResource(Res.string.extra_match_pick)) }
    }
}
