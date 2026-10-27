package com.kikepb.club.presentation.schedule

import com.kikepb.core.designsystem.triangulacion.preview.ScreenPreview
import com.kikepb.core.designsystem.triangulacion.preview.LARGEST_FONT_SCALE
import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.selection.toggleable
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import org.jetbrains.compose.resources.StringResource
import com.kikepb.core.designsystem.triangulacion.components.TriButton
import com.kikepb.core.designsystem.triangulacion.components.TriButtonVariant
import com.kikepb.core.designsystem.triangulacion.components.TriCaps
import com.kikepb.core.designsystem.triangulacion.components.TriCard
import com.kikepb.core.designsystem.triangulacion.components.TriChip
import com.kikepb.core.designsystem.triangulacion.components.TriIcon
import com.kikepb.core.designsystem.triangulacion.components.TriIconView
import com.kikepb.core.designsystem.triangulacion.components.TriNotice
import com.kikepb.core.designsystem.triangulacion.components.TriNoticeTone
import com.kikepb.core.designsystem.triangulacion.components.TriRoundIconButton
import com.kikepb.core.designsystem.triangulacion.components.TriTimePickerSheet
import com.kikepb.core.designsystem.triangulacion.theme.Radius
import com.kikepb.core.designsystem.triangulacion.theme.Space
import com.kikepb.core.designsystem.triangulacion.theme.Triangulacion
import squadfy_app.feature.club.presentation.generated.resources.day_initial_friday
import squadfy_app.feature.club.presentation.generated.resources.day_initial_monday
import squadfy_app.feature.club.presentation.generated.resources.day_initial_saturday
import squadfy_app.feature.club.presentation.generated.resources.day_initial_sunday
import squadfy_app.feature.club.presentation.generated.resources.day_initial_thursday
import squadfy_app.feature.club.presentation.generated.resources.day_initial_tuesday
import squadfy_app.feature.club.presentation.generated.resources.day_initial_wednesday
import squadfy_app.feature.club.presentation.generated.resources.schedule_days_before_one
import squadfy_app.feature.club.presentation.generated.resources.schedule_days_before_value
import squadfy_app.feature.club.presentation.generated.resources.schedule_days_before_zero
import squadfy_app.feature.club.presentation.generated.resources.schedule_decrease_days
import squadfy_app.feature.club.presentation.generated.resources.schedule_increase_days
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
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberDatePickerState
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
import squadfy_app.feature.club.presentation.generated.resources.exceptions_reason_hint
import squadfy_app.feature.club.presentation.generated.resources.time_picker_close
import squadfy_app.feature.club.presentation.generated.resources.time_picker_draw
import squadfy_app.feature.club.presentation.generated.resources.time_picker_match
import squadfy_app.feature.club.presentation.generated.resources.time_picker_reschedule
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
                if (state.schedule != null) ExceptionsSection(state = state, onAction = onAction)
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
        // Spec 018: one box per weekday with its initial; the selected one in Volt
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
            DayOfWeek.entries.forEach { day ->
                DayBox(
                    initial = stringResource(day.initial),
                    description = stringResource(day.label),
                    selected = form.dayOfWeek == day,
                    onClick = { onAction(ScheduleAction.OnDaySelected(day)) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
        Label(text = stringResource(Res.string.schedule_time))
        ValueBox(icon = TriIcon.Clock, label = stringResource(Res.string.schedule_time), value = form.matchTime.formatted()) {
            onAction(ScheduleAction.OnOpenTimePicker(TimeTarget.MATCH))
        }
        SquadfyTextField(state = state.timeZone, title = stringResource(Res.string.schedule_time_zone), singleLine = true)
        Label(text = stringResource(Res.string.schedule_format))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            MatchFormat.entries.forEach { format ->
                TriChip(text = stringResource(format.label), selected = form.format == format, onClick = { onAction(ScheduleAction.OnFormatSelected(format)) })
            }
        }
        SquadfyTextField(
            state = state.duration,
            title = stringResource(Res.string.schedule_duration),
            singleLine = true,
            keyboardType = KeyboardType.Number,
            leadingIcon = TriIcon.Ball
        )
        if (state.customDeadlinesEnabled) {
            HorizontalDivider(color = Triangulacion.colors.line)
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
            // One toggle with its label for screen readers
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .toggleable(value = form.isActive, role = Role.Switch, onValueChange = { onAction(ScheduleAction.OnActiveChanged(it)) })
            ) {
                Text(text = stringResource(Res.string.schedule_active), modifier = Modifier.weight(1f))
                Switch(checked = form.isActive, onCheckedChange = null)
            }
        }
        state.formError?.let { TriNotice(title = it.asString(), tone = TriNoticeTone.Danger) }
        TriButton(
            text = stringResource(if (state.schedule == null) Res.string.schedule_save_create else Res.string.schedule_save_update),
            onClick = { onAction(ScheduleAction.OnSave) },
            isLoading = state.isWorking,
            large = true,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/** Close / draw deadline: «1 día antes · 22:00» with a stepper for the days and the time as a chip. */
@Composable
private fun DeadlineEditor(title: String, days: Int, time: LocalTime, onDaysChange: (Int) -> Unit, onPickTime: () -> Unit) {
    val colors = Triangulacion.colors
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(text = title, style = Triangulacion.type.title, color = colors.ink)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = when (days) {
                    0 -> stringResource(Res.string.schedule_days_before_zero)
                    1 -> stringResource(Res.string.schedule_days_before_one)
                    else -> stringResource(Res.string.schedule_days_before_value, days)
                },
                style = Triangulacion.type.body,
                color = colors.inkMuted,
                modifier = Modifier.weight(1f)
            )
            TriRoundIconButton(icon = TriIcon.Minus, contentDescription = stringResource(Res.string.schedule_decrease_days), onClick = { if (days > 0) onDaysChange(days - 1) }, filled = true)
            TriRoundIconButton(icon = TriIcon.Plus, contentDescription = stringResource(Res.string.schedule_increase_days), onClick = { if (days < 6) onDaysChange(days + 1) }, filled = true)
            TriChip(
                text = time.formatted(),
                selected = false,
                onClick = onPickTime,
                icon = TriIcon.Clock,
                role = Role.Button,
                contentDescription = "$title: ${time.formatted()}"
            )
        }
    }
}

@Composable
private fun DayBox(initial: String, description: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = Triangulacion.colors
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .heightIn(min = 48.dp)
            .background(if (selected) colors.volt else colors.surface200, RoundedCornerShape(Radius.sm))
            .border(1.5.dp, if (selected) colors.volt else colors.lineStrong, RoundedCornerShape(Radius.sm))
            .clickable(role = Role.RadioButton, onClick = onClick)
            .semantics { contentDescription = description; this.selected = selected }
    ) {
        Text(text = initial, style = Triangulacion.type.title, color = if (selected) colors.onVolt else colors.ink)
    }
}

/** Read-only field that opens a picker (time); read as «Hora: 20:00». */
@Composable
private fun ValueBox(icon: TriIcon, label: String, value: String, onClick: () -> Unit) {
    val colors = Triangulacion.colors
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .background(colors.surface200, RoundedCornerShape(Radius.sm))
            .border(1.5.dp, colors.lineStrong, RoundedCornerShape(Radius.sm))
            .clickable(role = Role.Button, onClick = onClick)
            .semantics(mergeDescendants = true) { contentDescription = "$label: $value" }
            .padding(horizontal = 14.dp)
    ) {
        TriIconView(icon = icon, contentDescription = null, size = 20.dp, tint = colors.inkMuted)
        Text(text = value, style = Triangulacion.type.body.copy(fontSize = Triangulacion.type.bodyLg.fontSize), color = colors.ink)
    }
}

private val DayOfWeek.initial: StringResource
    get() = when (this) {
        DayOfWeek.MONDAY -> Res.string.day_initial_monday
        DayOfWeek.TUESDAY -> Res.string.day_initial_tuesday
        DayOfWeek.WEDNESDAY -> Res.string.day_initial_wednesday
        DayOfWeek.THURSDAY -> Res.string.day_initial_thursday
        DayOfWeek.FRIDAY -> Res.string.day_initial_friday
        DayOfWeek.SATURDAY -> Res.string.day_initial_saturday
        DayOfWeek.SUNDAY -> Res.string.day_initial_sunday
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
                TriButton(text = stringResource(Res.string.exceptions_add), onClick = { onAction(ScheduleAction.OnOpenExceptionForm) }, variant = TriButtonVariant.Secondary, modifier = Modifier.fillMaxWidth())
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
        TriButton(text = form.date?.toString() ?: stringResource(Res.string.exceptions_pick_date), onClick = { onAction(ScheduleAction.OnOpenDatePicker(DateTarget.EXCEPTION)) }, variant = TriButtonVariant.Secondary)
    }
    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        TriChip(text = stringResource(Res.string.exceptions_type_cancel), selected = form.type == ScheduleExceptionType.CANCELLED, onClick = { onAction(ScheduleAction.OnExceptionTypeSelected(ScheduleExceptionType.CANCELLED)) })
        TriChip(text = stringResource(Res.string.exceptions_type_reschedule), selected = form.type == ScheduleExceptionType.RESCHEDULED, onClick = { onAction(ScheduleAction.OnExceptionTypeSelected(ScheduleExceptionType.RESCHEDULED)) })
    }
    if (form.type == ScheduleExceptionType.RESCHEDULED) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TriButton(text = form.newDate?.toString() ?: stringResource(Res.string.exceptions_new_date), onClick = { onAction(ScheduleAction.OnOpenDatePicker(DateTarget.RESCHEDULE)) }, variant = TriButtonVariant.Secondary, modifier = Modifier.weight(1f))
            TriButton(text = form.newTime?.formatted() ?: stringResource(Res.string.exceptions_new_time), onClick = { onAction(ScheduleAction.OnOpenTimePicker(TimeTarget.RESCHEDULE)) }, variant = TriButtonVariant.Secondary, modifier = Modifier.weight(1f))
        }
    }
    // AC-020-08: the reason is mandatory; members read it in the Match tab and in the push
    SquadfyTextField(
        state = form.reason,
        title = stringResource(Res.string.exceptions_reason),
        placeholder = stringResource(Res.string.exceptions_reason_hint),
        singleLine = true
    )
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
            // Spec 020 AC-020-10: the brand's time picker instead of Material's clock
            TriTimePickerSheet(
                title = stringResource(
                    when (dialog.target) {
                        TimeTarget.MATCH -> Res.string.time_picker_match
                        TimeTarget.CLOSE -> Res.string.time_picker_close
                        TimeTarget.DRAW -> Res.string.time_picker_draw
                        TimeTarget.RESCHEDULE -> Res.string.time_picker_reschedule
                    }
                ),
                initial = initial,
                onConfirm = { onAction(ScheduleAction.OnTimePicked(dialog.target, it)) },
                onDismiss = dismiss
            )
        }
        is ScheduleDialog.DatePicker -> {
            val matchDay = state.schedule?.dayOfWeek
            val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
            val week = state.exceptionForm?.date?.let { ExceptionForm.weekOf(it) }
            val selectable = remember(dialog.target, matchDay, today, week) {
                object : SelectableDates {
                    override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                        val date = LocalDate.fromEpochDays((utcTimeMillis / MILLIS_PER_DAY).toInt())
                        // BE-008 RN-B1: the excepted week is a future match day; D-20: the new date is in that same week
                        return date >= today && when (dialog.target) {
                            DateTarget.EXCEPTION -> date > today && date.dayOfWeek == matchDay
                            DateTarget.RESCHEDULE -> week == null || date in week
                        }
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
    TriCard(modifier = Modifier.fillMaxWidth(), contentPadding = Space.s5) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) { content() }
    }
}

@Composable
private fun Label(text: String) {
    TriCaps(text = text)
}

@Composable
private fun Hint(text: String) {
    Text(text = text, style = Triangulacion.type.bodySm, color = Triangulacion.colors.inkMuted)
}

private const val MILLIS_PER_DAY = 24L * 60 * 60 * 1000

// Previews (spec 018): Tiza and Noche with the default state, no sample data

@Preview(name = "11 Horario semanal · Tiza", widthDp = 393, heightDp = 852)
@Composable
private fun ScheduleTizaPreview() = ScreenPreview { ScheduleScreen(state = ScheduleState(), onAction = {}, onBackClick = {}, snackbarHostState = remember { SnackbarHostState() }) }

@Preview(name = "11 Horario semanal · Noche", widthDp = 393, heightDp = 852)
@Composable
private fun ScheduleNochePreview() = ScreenPreview(darkTheme = true) { ScheduleScreen(state = ScheduleState(), onAction = {}, onBackClick = {}, snackbarHostState = remember { SnackbarHostState() }) }

@Preview(name = "11 Horario semanal · Tiza · 200 %", widthDp = 393, heightDp = 852)
@Composable
private fun ScheduleLargeFontPreview() = ScreenPreview(fontScale = LARGEST_FONT_SCALE) { ScheduleScreen(state = ScheduleState(), onAction = {}, onBackClick = {}, snackbarHostState = remember { SnackbarHostState() }) }
