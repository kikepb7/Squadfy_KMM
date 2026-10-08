package com.kikepb.club.presentation.schedule

import com.kikepb.core.designsystem.components.loading.SquadfyLoadingIndicator
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kikepb.club.domain.model.ClubScheduleModel
import com.kikepb.club.domain.model.MatchFormat
import com.kikepb.club.domain.model.ScheduleDraft
import com.kikepb.club.domain.model.ScheduleExceptionModel
import com.kikepb.club.domain.model.ScheduleExceptionType
import com.kikepb.club.presentation.mapper.formatted
import com.kikepb.club.presentation.mapper.label
import com.kikepb.club.presentation.mapper.shortLabel
import com.kikepb.club.presentation.util.formatDateTime
import com.kikepb.core.designsystem.components.buttons.SquadfyButton
import com.kikepb.core.designsystem.components.dialogs.SquadfyDestructiveConfirmationDialog
import com.kikepb.core.designsystem.components.textfields.SquadfyTextField
import com.kikepb.core.designsystem.components.topbar.SquadfyTopBar
import com.kikepb.core.designsystem.theme.extended
import com.kikepb.core.presentation.util.ObserveAsEvents
import kotlinx.coroutines.launch
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import squadfy_app.feature.club.presentation.generated.resources.Res
import squadfy_app.feature.club.presentation.generated.resources.common_cancel
import squadfy_app.feature.club.presentation.generated.resources.common_confirm
import squadfy_app.feature.club.presentation.generated.resources.common_save
import squadfy_app.feature.club.presentation.generated.resources.exceptions_add
import squadfy_app.feature.club.presentation.generated.resources.exceptions_cancelled
import squadfy_app.feature.club.presentation.generated.resources.exceptions_date
import squadfy_app.feature.club.presentation.generated.resources.exceptions_delete_confirm
import squadfy_app.feature.club.presentation.generated.resources.exceptions_delete_description
import squadfy_app.feature.club.presentation.generated.resources.exceptions_delete_title
import squadfy_app.feature.club.presentation.generated.resources.exceptions_empty
import squadfy_app.feature.club.presentation.generated.resources.exceptions_new_date
import squadfy_app.feature.club.presentation.generated.resources.exceptions_new_time
import squadfy_app.feature.club.presentation.generated.resources.exceptions_pick_date
import squadfy_app.feature.club.presentation.generated.resources.exceptions_reason
import squadfy_app.feature.club.presentation.generated.resources.exceptions_rescheduled
import squadfy_app.feature.club.presentation.generated.resources.exceptions_title
import squadfy_app.feature.club.presentation.generated.resources.exceptions_type_cancel
import squadfy_app.feature.club.presentation.generated.resources.exceptions_type_reschedule
import squadfy_app.feature.club.presentation.generated.resources.schedule_active
import squadfy_app.feature.club.presentation.generated.resources.schedule_close
import squadfy_app.feature.club.presentation.generated.resources.schedule_day
import squadfy_app.feature.club.presentation.generated.resources.schedule_days_before
import squadfy_app.feature.club.presentation.generated.resources.schedule_deactivate_confirm
import squadfy_app.feature.club.presentation.generated.resources.schedule_deactivate_description
import squadfy_app.feature.club.presentation.generated.resources.schedule_deactivate_title
import squadfy_app.feature.club.presentation.generated.resources.schedule_deadline_days_before
import squadfy_app.feature.club.presentation.generated.resources.schedule_deadline_same_day
import squadfy_app.feature.club.presentation.generated.resources.schedule_draw
import squadfy_app.feature.club.presentation.generated.resources.schedule_duration
import squadfy_app.feature.club.presentation.generated.resources.schedule_format
import squadfy_app.feature.club.presentation.generated.resources.schedule_inactive
import squadfy_app.feature.club.presentation.generated.resources.schedule_none
import squadfy_app.feature.club.presentation.generated.resources.schedule_none_manager
import squadfy_app.feature.club.presentation.generated.resources.schedule_offline
import squadfy_app.feature.club.presentation.generated.resources.schedule_save_create
import squadfy_app.feature.club.presentation.generated.resources.schedule_save_update
import squadfy_app.feature.club.presentation.generated.resources.schedule_summary
import squadfy_app.feature.club.presentation.generated.resources.schedule_summary_deadlines
import squadfy_app.feature.club.presentation.generated.resources.schedule_time
import squadfy_app.feature.club.presentation.generated.resources.schedule_time_zone
import squadfy_app.feature.club.presentation.generated.resources.schedule_title
import kotlin.time.Clock

@Composable
fun ScheduleRoot(
    onBackClick: () -> Unit,
    viewModel: ScheduleViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is ScheduleEvent.ShowMessage -> scope.launch { snackbarHostState.showSnackbar(event.message.asStringAsync()) }
            ScheduleEvent.ScheduleChanged -> Unit
        }
    }

    ScheduleScreen(state = state, onAction = viewModel::onAction, onBackClick = onBackClick, snackbarHostState = snackbarHostState)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    state: ScheduleState,
    onAction: (ScheduleAction) -> Unit,
    onBackClick: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.extended.surfaceLower,
        topBar = { SquadfyTopBar(title = stringResource(Res.string.schedule_title), onBackClick = onBackClick) },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = { onAction(ScheduleAction.OnRefresh) },
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (state.isOffline) Hint(text = stringResource(Res.string.schedule_offline))
                ScheduleSummaryCard(state = state)
                if (state.canEdit) ScheduleForm(state = state, onAction = onAction)
                if (state.exceptionsEnabled && state.schedule != null) ExceptionsSection(state = state, onAction = onAction)
            }
        }
    }

    ScheduleDialogs(state = state, onAction = onAction)
}

@Composable
private fun ScheduleSummaryCard(state: ScheduleState) {
    Card {
        val schedule = state.schedule
        when {
            schedule == null && state.isLoading -> SquadfyLoadingIndicator()
            schedule == null -> Text(
                text = stringResource(if (state.canEdit) Res.string.schedule_none_manager else Res.string.schedule_none),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.extended.textSecondary
            )
            schedule != null -> {
                Text(
                    text = scheduleSummary(schedule),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.extended.textPrimary
                )
                if (state.customDeadlinesEnabled) {
                    Text(
                        text = stringResource(
                            Res.string.schedule_summary_deadlines,
                            deadline(schedule.closeDaysBefore, schedule.closeTime),
                            deadline(schedule.drawDaysBefore, schedule.drawTime)
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.extended.textPlaceholder
                    )
                }
                if (!schedule.isActive) Hint(text = stringResource(Res.string.schedule_inactive))
            }
        }
    }
}

/** "Jueves 20:00 (Europe/Madrid) · 5v5 · 60 min" (AC-004-03). */
@Composable
fun scheduleSummary(schedule: ClubScheduleModel): String = stringResource(
    Res.string.schedule_summary,
    stringResource(schedule.dayOfWeek.label),
    schedule.matchTime.formatted(),
    schedule.timeZone,
    stringResource(schedule.format.shortLabel),
    schedule.durationMinutes
)

@Composable
private fun deadline(daysBefore: Int, time: LocalTime): String =
    if (daysBefore == 0) stringResource(Res.string.schedule_deadline_same_day, time.formatted())
    else stringResource(Res.string.schedule_deadline_days_before, daysBefore, time.formatted())

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ScheduleForm(state: ScheduleState, onAction: (ScheduleAction) -> Unit) {
    val form = state.form ?: return
    Card {
        Label(text = stringResource(Res.string.schedule_day))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            DayOfWeek.entries.forEach { day ->
                FilterChip(
                    selected = form.dayOfWeek == day,
                    onClick = { onAction(ScheduleAction.OnDaySelected(day)) },
                    label = { Text(text = stringResource(day.label).take(3)) }
                )
            }
        }
        TimeRow(label = stringResource(Res.string.schedule_time), time = form.matchTime) {
            onAction(ScheduleAction.OnOpenTimePicker(TimeTarget.MATCH))
        }
        SquadfyTextField(state = state.timeZone, title = stringResource(Res.string.schedule_time_zone), singleLine = true)
        Label(text = stringResource(Res.string.schedule_format))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            MatchFormat.entries.forEach { format ->
                FilterChip(
                    selected = form.format == format,
                    onClick = { onAction(ScheduleAction.OnFormatSelected(format)) },
                    label = { Text(text = stringResource(format.label)) }
                )
            }
        }
        SquadfyTextField(
            state = state.duration,
            title = stringResource(Res.string.schedule_duration),
            singleLine = true,
            keyboardType = KeyboardType.Number
        )
        if (state.customDeadlinesEnabled) {
            HorizontalDivider(color = MaterialTheme.colorScheme.extended.surfaceOutline)
            DeadlineEditor(
                title = stringResource(Res.string.schedule_close),
                days = form.closeDaysBefore,
                time = form.closeTime,
                onDaysChange = { onAction(ScheduleAction.OnCloseDaysChanged(it)) },
                onPickTime = { onAction(ScheduleAction.OnOpenTimePicker(TimeTarget.CLOSE)) }
            )
            DeadlineEditor(
                title = stringResource(Res.string.schedule_draw),
                days = form.drawDaysBefore,
                time = form.drawTime,
                onDaysChange = { onAction(ScheduleAction.OnDrawDaysChanged(it)) },
                onPickTime = { onAction(ScheduleAction.OnOpenTimePicker(TimeTarget.DRAW)) }
            )
        }
        if (state.schedule != null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = stringResource(Res.string.schedule_active), modifier = Modifier.weight(1f))
                Switch(checked = form.isActive, onCheckedChange = { onAction(ScheduleAction.OnActiveChanged(it)) })
            }
        }
        state.formError?.let { Text(text = it.asString(), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
        SquadfyButton(
            text = stringResource(if (state.schedule == null) Res.string.schedule_save_create else Res.string.schedule_save_update),
            onClick = { onAction(ScheduleAction.OnSave) },
            isLoading = state.isWorking,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun DeadlineEditor(title: String, days: Int, time: LocalTime, onDaysChange: (Int) -> Unit, onPickTime: () -> Unit) {
    Label(text = title)
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = stringResource(Res.string.schedule_days_before), style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
        TextButton(onClick = { onDaysChange(days - 1) }, enabled = days > 0) { Text("−") }
        Text(text = days.toString(), style = MaterialTheme.typography.titleSmall)
        TextButton(onClick = { onDaysChange(days + 1) }, enabled = days < 6) { Text("+") }
        OutlinedButton(onClick = onPickTime) { Text(text = time.formatted()) }
    }
}

@Composable
private fun TimeRow(label: String, time: LocalTime, onClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
        OutlinedButton(onClick = onClick) { Text(text = time.formatted()) }
    }
}

@Composable
private fun ExceptionsSection(state: ScheduleState, onAction: (ScheduleAction) -> Unit) {
    val timeZone = state.schedule?.timeZone ?: return
    Card {
        Text(
            text = stringResource(Res.string.exceptions_title),
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.extended.textPrimary
        )
        if (state.exceptions.isEmpty()) {
            Text(
                text = stringResource(Res.string.exceptions_empty),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.extended.textPlaceholder
            )
        }
        state.exceptions.forEach { exception ->
            ExceptionRow(exception = exception, timeZone = timeZone, canDelete = state.canEdit, onDelete = {
                onAction(ScheduleAction.OnDeleteExceptionClick(exception))
            })
        }
        if (state.canEdit) {
            val form = state.exceptionForm
            if (form == null) {
                OutlinedButton(onClick = { onAction(ScheduleAction.OnOpenExceptionForm) }, modifier = Modifier.fillMaxWidth()) {
                    Text(text = stringResource(Res.string.exceptions_add))
                }
            } else {
                ExceptionFormContent(form = form, isWorking = state.isWorking, onAction = onAction)
            }
        }
    }
}

@Composable
private fun ExceptionRow(exception: ScheduleExceptionModel, timeZone: String, canDelete: Boolean, onDelete: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            val date = exception.date.toString()
            Text(
                text = when (exception.type) {
                    ScheduleExceptionType.CANCELLED -> stringResource(Res.string.exceptions_cancelled, date)
                    ScheduleExceptionType.RESCHEDULED -> stringResource(
                        Res.string.exceptions_rescheduled,
                        date,
                        exception.newScheduledAt?.let { formatDateTime(it, timeZone) }.orEmpty()
                    )
                },
                style = MaterialTheme.typography.bodyMedium
            )
            exception.reason?.let {
                Text(text = it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.extended.textPlaceholder)
            }
        }
        if (canDelete) {
            IconButton(onClick = onDelete) {
                Icon(imageVector = Icons.Outlined.Delete, contentDescription = stringResource(Res.string.exceptions_delete_confirm))
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ExceptionFormContent(form: ExceptionForm, isWorking: Boolean, onAction: (ScheduleAction) -> Unit) {
    HorizontalDivider(color = MaterialTheme.colorScheme.extended.surfaceOutline)
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = stringResource(Res.string.exceptions_date), modifier = Modifier.weight(1f))
        OutlinedButton(onClick = { onAction(ScheduleAction.OnOpenDatePicker(DateTarget.EXCEPTION)) }) {
            Text(text = form.date?.toString() ?: stringResource(Res.string.exceptions_pick_date))
        }
    }
    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        FilterChip(
            selected = form.type == ScheduleExceptionType.CANCELLED,
            onClick = { onAction(ScheduleAction.OnExceptionTypeSelected(ScheduleExceptionType.CANCELLED)) },
            label = { Text(text = stringResource(Res.string.exceptions_type_cancel)) }
        )
        FilterChip(
            selected = form.type == ScheduleExceptionType.RESCHEDULED,
            onClick = { onAction(ScheduleAction.OnExceptionTypeSelected(ScheduleExceptionType.RESCHEDULED)) },
            label = { Text(text = stringResource(Res.string.exceptions_type_reschedule)) }
        )
    }
    if (form.type == ScheduleExceptionType.RESCHEDULED) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { onAction(ScheduleAction.OnOpenDatePicker(DateTarget.RESCHEDULE)) }, modifier = Modifier.weight(1f)) {
                Text(text = form.newDate?.toString() ?: stringResource(Res.string.exceptions_new_date))
            }
            OutlinedButton(onClick = { onAction(ScheduleAction.OnOpenTimePicker(TimeTarget.RESCHEDULE)) }, modifier = Modifier.weight(1f)) {
                Text(text = form.newTime?.formatted() ?: stringResource(Res.string.exceptions_new_time))
            }
        }
    }
    SquadfyTextField(state = form.reason, title = stringResource(Res.string.exceptions_reason), singleLine = true)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        TextButton(onClick = { onAction(ScheduleAction.OnCloseExceptionForm) }) { Text(text = stringResource(Res.string.common_cancel)) }
        SquadfyButton(
            text = stringResource(Res.string.common_save),
            onClick = { onAction(ScheduleAction.OnSaveException) },
            enabled = form.canSave,
            isLoading = isWorking,
            modifier = Modifier.weight(1f)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScheduleDialogs(state: ScheduleState, onAction: (ScheduleAction) -> Unit) {
    val dismiss = { onAction(ScheduleAction.OnDismissDialog) }
    when (val dialog = state.dialog) {
        is ScheduleDialog.TimePicker -> {
            val form = state.form
            val initial = when (dialog.target) {
                TimeTarget.MATCH -> form?.matchTime
                TimeTarget.CLOSE -> form?.closeTime
                TimeTarget.DRAW -> form?.drawTime
                TimeTarget.RESCHEDULE -> state.exceptionForm?.newTime ?: form?.matchTime
            } ?: LocalTime(20, 0)
            val pickerState = rememberTimePickerState(initialHour = initial.hour, initialMinute = initial.minute, is24Hour = true)
            AlertDialog(
                onDismissRequest = dismiss,
                text = { TimePicker(state = pickerState) },
                confirmButton = {
                    TextButton(onClick = {
                        onAction(ScheduleAction.OnTimePicked(dialog.target, LocalTime(pickerState.hour, pickerState.minute)))
                    }) { Text(text = stringResource(Res.string.common_confirm)) }
                },
                dismissButton = { TextButton(onClick = dismiss) { Text(text = stringResource(Res.string.common_cancel)) } }
            )
        }
        is ScheduleDialog.DatePicker -> {
            val matchDay = state.schedule?.dayOfWeek
            val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
            val selectable = remember(dialog.target, matchDay, today) {
                object : SelectableDates {
                    override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                        val date = LocalDate.fromEpochDays((utcTimeMillis / MILLIS_PER_DAY).toInt())
                        // BE-008 RN-B1: the excepted week is a future match day; the new date is any future day
                        return date > today && (dialog.target == DateTarget.RESCHEDULE || date.dayOfWeek == matchDay)
                    }
                }
            }
            val pickerState = rememberDatePickerState(selectableDates = selectable)
            DatePickerDialog(
                onDismissRequest = dismiss,
                confirmButton = {
                    TextButton(
                        onClick = {
                            pickerState.selectedDateMillis?.let { millis ->
                                onAction(ScheduleAction.OnDatePicked(dialog.target, LocalDate.fromEpochDays((millis / MILLIS_PER_DAY).toInt())))
                            }
                        },
                        enabled = pickerState.selectedDateMillis != null
                    ) { Text(text = stringResource(Res.string.common_confirm)) }
                },
                dismissButton = { TextButton(onClick = dismiss) { Text(text = stringResource(Res.string.common_cancel)) } }
            ) {
                DatePicker(state = pickerState)
            }
        }
        ScheduleDialog.ConfirmDeactivate -> SquadfyDestructiveConfirmationDialog(
            title = stringResource(Res.string.schedule_deactivate_title),
            description = stringResource(Res.string.schedule_deactivate_description),
            confirmButtonText = stringResource(Res.string.schedule_deactivate_confirm),
            cancelButtonText = stringResource(Res.string.common_cancel),
            onConfirmClick = { onAction(ScheduleAction.OnConfirmDeactivate) },
            onCancelClick = dismiss,
            onDismiss = dismiss
        )
        is ScheduleDialog.ConfirmDeleteException -> SquadfyDestructiveConfirmationDialog(
            title = stringResource(Res.string.exceptions_delete_title),
            description = stringResource(Res.string.exceptions_delete_description),
            confirmButtonText = stringResource(Res.string.exceptions_delete_confirm),
            cancelButtonText = stringResource(Res.string.common_cancel),
            onConfirmClick = { onAction(ScheduleAction.OnConfirmDeleteException) },
            onCancelClick = dismiss,
            onDismiss = dismiss
        )
        null -> Unit
    }
}

@Composable
private fun Card(content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) { content() }
    }
}

@Composable
private fun Label(text: String) {
    Text(text = text, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.extended.textSecondary)
}

@Composable
private fun Hint(text: String) {
    Text(text = text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.extended.textPlaceholder)
}

private const val MILLIS_PER_DAY = 24L * 60 * 60 * 1000
