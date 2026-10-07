package com.kikepb.club.presentation.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import org.jetbrains.compose.resources.stringResource
import squadfy_app.feature.club.presentation.generated.resources.Res
import squadfy_app.feature.club.presentation.generated.resources.common_cancel
import squadfy_app.feature.club.presentation.generated.resources.common_confirm

private const val MILLIS_PER_DAY = 86_400_000L

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TimePickerDialog(initial: LocalTime, onConfirm: (LocalTime) -> Unit, onDismiss: () -> Unit) {
    val pickerState = rememberTimePickerState(initialHour = initial.hour, initialMinute = initial.minute, is24Hour = true)
    AlertDialog(
        onDismissRequest = onDismiss,
        text = { TimePicker(state = pickerState) },
        confirmButton = {
            TextButton(onClick = { onConfirm(LocalTime(pickerState.hour, pickerState.minute)) }) { Text(text = stringResource(Res.string.common_confirm)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(text = stringResource(Res.string.common_cancel)) } }
    )
}

/** Date picker in local dates; [isSelectable] decides which days can be picked. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DatePickerDialog(isSelectable: (LocalDate) -> Boolean, onConfirm: (LocalDate) -> Unit, onDismiss: () -> Unit) {
    val selectable = remember(isSelectable) {
        object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean = isSelectable(LocalDate.fromEpochDays((utcTimeMillis / MILLIS_PER_DAY).toInt()))
        }
    }
    val pickerState = rememberDatePickerState(selectableDates = selectable)
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = { pickerState.selectedDateMillis?.let { onConfirm(LocalDate.fromEpochDays((it / MILLIS_PER_DAY).toInt())) } },
                enabled = pickerState.selectedDateMillis != null
            ) { Text(text = stringResource(Res.string.common_confirm)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(text = stringResource(Res.string.common_cancel)) } }
    ) {
        DatePicker(state = pickerState)
    }
}
