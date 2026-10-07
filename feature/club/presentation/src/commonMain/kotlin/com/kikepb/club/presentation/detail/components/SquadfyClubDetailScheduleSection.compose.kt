package com.kikepb.club.presentation.detail.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kikepb.club.domain.model.ClubModel
import com.kikepb.club.presentation.settings.ScheduleSettingsViewModel
import com.kikepb.core.designsystem.components.buttons.SquadfyButton
import com.kikepb.core.designsystem.components.buttons.SquadfyButtonStyle
import com.kikepb.core.designsystem.components.textfields.SquadfyTextField
import com.kikepb.core.designsystem.theme.extended
import org.koin.compose.viewmodel.koinViewModel

private val DAYS_OF_WEEK = listOf(
    "MONDAY" to "L",
    "TUESDAY" to "M",
    "WEDNESDAY" to "X",
    "THURSDAY" to "J",
    "FRIDAY" to "V",
    "SATURDAY" to "S",
    "SUNDAY" to "D"
)

@Composable
fun SquadfyClubDetailScheduleSection(
    club: ClubModel,
    viewModel: ScheduleSettingsViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val scheduleKey = "${club.matchDayOfWeek}-${club.matchStartTime}-${club.matchEndTime}-${club.seasonStartMonth}-${club.seasonStartDay}-${club.drawTime}"
    var selectedDay by rememberSaveable(scheduleKey) { mutableStateOf(club.matchDayOfWeek) }
    val startTimeState = remember(scheduleKey) { TextFieldState(initialText = club.matchStartTime.orEmpty()) }
    val endTimeState = remember(scheduleKey) { TextFieldState(initialText = club.matchEndTime.orEmpty()) }
    val seasonMonthState = remember(scheduleKey) { TextFieldState(initialText = club.seasonStartMonth.toString()) }
    val seasonDayState = remember(scheduleKey) { TextFieldState(initialText = club.seasonStartDay.toString()) }
    val drawTimeState = remember(scheduleKey) { TextFieldState(initialText = club.drawTime) }
    val newExceptionDateState = remember { TextFieldState() }
    val newExceptionReasonState = remember { TextFieldState() }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SquadfyClubDetailTabSectionTitle("Horario semanal")
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 1.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Día del partido",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.extended.textPlaceholder
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        DAYS_OF_WEEK.forEach { (value, label) ->
                            DayChip(
                                label = label,
                                selected = selectedDay == value,
                                onClick = { selectedDay = if (selectedDay == value) null else value }
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.extended.surfaceOutline)

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        SquadfyTextField(
                            state = startTimeState,
                            modifier = Modifier.weight(1f),
                            title = "Hora inicio",
                            placeholder = "10:00",
                            singleLine = true,
                            keyboardType = KeyboardType.Text
                        )
                        SquadfyTextField(
                            state = endTimeState,
                            modifier = Modifier.weight(1f),
                            title = "Hora fin",
                            placeholder = "12:00",
                            singleLine = true,
                            keyboardType = KeyboardType.Text
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.extended.surfaceOutline)

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        SquadfyTextField(
                            state = seasonMonthState,
                            modifier = Modifier.weight(1f),
                            title = "Mes inicio temporada",
                            placeholder = "9",
                            singleLine = true,
                            keyboardType = KeyboardType.Number
                        )
                        SquadfyTextField(
                            state = seasonDayState,
                            modifier = Modifier.weight(1f),
                            title = "Día inicio temporada",
                            placeholder = "1",
                            singleLine = true,
                            keyboardType = KeyboardType.Number
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.extended.surfaceOutline)

                    SquadfyTextField(
                        state = drawTimeState,
                        modifier = Modifier.fillMaxWidth(),
                        title = "Hora del sorteo (sábado)",
                        placeholder = "18:00",
                        singleLine = true,
                        keyboardType = KeyboardType.Text,
                        supportingText = "El sorteo automático de equipos se hace el sábado a esta hora"
                    )

                    state.message?.let { message ->
                        Text(text = message.asString(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                    }

                    SquadfyButton(
                        text = "Guardar horario",
                        onClick = {
                            viewModel.saveSchedule(
                                matchDayOfWeek = selectedDay,
                                matchStartTime = startTimeState.text.toString().trim().ifBlank { null },
                                matchEndTime = endTimeState.text.toString().trim().ifBlank { null },
                                seasonStartMonth = seasonMonthState.text.toString().trim().toIntOrNull(),
                                seasonStartDay = seasonDayState.text.toString().trim().toIntOrNull(),
                                drawTime = drawTimeState.text.toString().trim().ifBlank { null }
                            )
                        },
                        isLoading = state.isSaving,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SquadfyClubDetailTabSectionTitle("Días excepcionales (vacaciones, festivos)")
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (state.exceptions.isEmpty()) {
                        Text(
                            text = "No hay excepciones configuradas.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.extended.textPlaceholder
                        )
                    } else {
                        state.exceptions.forEach { exception ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = exception.date,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.extended.textPrimary
                                    )
                                    exception.reason?.let {
                                        Text(text = it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.extended.textPlaceholder)
                                    }
                                }
                                IconButton(onClick = { viewModel.removeException(exception.id) }) {
                                    Icon(imageVector = Icons.Outlined.Close, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.extended.surfaceOutline)
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        SquadfyTextField(
                            state = newExceptionDateState,
                            modifier = Modifier.weight(1f),
                            title = "Fecha",
                            placeholder = "2026-12-25",
                            singleLine = true
                        )
                        SquadfyTextField(
                            state = newExceptionReasonState,
                            modifier = Modifier.weight(1f),
                            title = "Motivo",
                            placeholder = "Navidad",
                            singleLine = true
                        )
                    }
                    SquadfyButton(
                        text = "Añadir excepción",
                        style = SquadfyButtonStyle.SECONDARY,
                        onClick = {
                            val date = newExceptionDateState.text.toString().trim()
                            viewModel.addException(date = date, reason = newExceptionReasonState.text.toString().trim().ifBlank { null })
                            newExceptionDateState.clearText()
                            newExceptionReasonState.clearText()
                        },
                        enabled = newExceptionDateState.text.isNotBlank(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun DayChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}
