package com.kikepb.core.designsystem.triangulacion.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kikepb.core.designsystem.triangulacion.theme.Radius
import com.kikepb.core.designsystem.triangulacion.theme.Space
import com.kikepb.core.designsystem.triangulacion.theme.Triangulacion
import kotlinx.datetime.LocalTime
import org.jetbrains.compose.resources.stringResource
import squadfy_app.core.designsystem.generated.resources.Res
import squadfy_app.core.designsystem.generated.resources.tri_time_cancel
import squadfy_app.core.designsystem.generated.resources.tri_time_common
import squadfy_app.core.designsystem.generated.resources.tri_time_hour_value
import squadfy_app.core.designsystem.generated.resources.tri_time_hours
import squadfy_app.core.designsystem.generated.resources.tri_time_minus
import squadfy_app.core.designsystem.generated.resources.tri_time_minute_value
import squadfy_app.core.designsystem.generated.resources.tri_time_minutes
import squadfy_app.core.designsystem.generated.resources.tri_time_plus
import squadfy_app.core.designsystem.generated.resources.tri_time_selected
import squadfy_app.core.designsystem.generated.resources.tri_time_use

/**
 * Time steps of [TriTimePickerSheet] (spec 020 AC-020-10): − and + move to the previous or next quarter hour and wrap
 * around midnight; the grid offers every hour and the minutes in steps of five.
 */
object TriTimeSteps {
    const val STEP_MINUTES = 15
    private const val MINUTES_PER_DAY = 24 * 60

    /** Usual kickoff times of an amateur club. */
    val COMMON = listOf(LocalTime(18, 0), LocalTime(19, 0), LocalTime(20, 0), LocalTime(21, 0), LocalTime(22, 0))
    val HOURS = (0..23).toList()
    val MINUTES = (0..55 step 5).toList()

    fun next(time: LocalTime): LocalTime = ofMinutes((time.totalMinutes / STEP_MINUTES + 1) * STEP_MINUTES)

    fun previous(time: LocalTime): LocalTime {
        val total = time.totalMinutes
        return ofMinutes(if (total % STEP_MINUTES == 0) total - STEP_MINUTES else total - total % STEP_MINUTES)
    }

    fun withHour(time: LocalTime, hour: Int): LocalTime = LocalTime(hour, time.minute)
    fun withMinute(time: LocalTime, minute: Int): LocalTime = LocalTime(time.hour, minute)

    fun format(time: LocalTime): String = "${time.hour.twoDigits()}:${time.minute.twoDigits()}"

    private val LocalTime.totalMinutes: Int get() = hour * 60 + minute
    private fun ofMinutes(total: Int): LocalTime {
        val wrapped = ((total % MINUTES_PER_DAY) + MINUTES_PER_DAY) % MINUTES_PER_DAY
        return LocalTime(wrapped / 60, wrapped % 60)
    }
}

private fun Int.twoDigits(): String = toString().padStart(2, '0')

private enum class TimePart { HOURS, MINUTES }

/**
 * Time picker of the brand (spec 020 AC-020-10), for the kickoff, the close, the draw and a moved week:
 * - the time in big figures; tapping the hours or the minutes opens their grid;
 * - − and + in quarters of an hour, and chips with the usual kickoff times;
 * - a single Volt CTA «Usar 20:00».
 * Screen readers hear the chosen time whenever it changes (live region); each cell is a radio button with its value.
 * The sheet scrolls, so it keeps working with the largest font sizes.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TriTimePickerSheet(
    title: String,
    initial: LocalTime,
    onConfirm: (LocalTime) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = Triangulacion.colors
    var time by remember { mutableStateOf(initial) }
    var part by rememberSaveable { mutableStateOf(TimePart.HOURS) }
    val formatted = TriTimeSteps.format(time)
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState, containerColor = colors.surface100) {
        Column(
            verticalArrangement = Arrangement.spacedBy(Space.s4),
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Space.s5)
                .padding(bottom = Space.s5)
                .navigationBarsPadding()
        ) {
            Text(text = title, style = Triangulacion.type.title, color = colors.ink, modifier = Modifier.semantics { heading() })

            val selectedDescription = stringResource(Res.string.tri_time_selected, formatted)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                TriRoundIconButton(
                    icon = TriIcon.Minus,
                    contentDescription = stringResource(Res.string.tri_time_minus),
                    onClick = { time = TriTimeSteps.previous(time) },
                    filled = true
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.semantics { contentDescription = selectedDescription; liveRegion = LiveRegionMode.Polite }
                ) {
                    TimeSegment(
                        value = time.hour.twoDigits(),
                        description = stringResource(Res.string.tri_time_hours),
                        selected = part == TimePart.HOURS,
                        onClick = { part = TimePart.HOURS }
                    )
                    Text(
                        text = ":",
                        style = Triangulacion.type.statXl,
                        color = colors.inkMuted,
                        modifier = Modifier.padding(horizontal = Space.s1).clearAndSetSemantics { }
                    )
                    TimeSegment(
                        value = time.minute.twoDigits(),
                        description = stringResource(Res.string.tri_time_minutes),
                        selected = part == TimePart.MINUTES,
                        onClick = { part = TimePart.MINUTES }
                    )
                }
                TriRoundIconButton(
                    icon = TriIcon.Plus,
                    contentDescription = stringResource(Res.string.tri_time_plus),
                    onClick = { time = TriTimeSteps.next(time) },
                    filled = true
                )
            }

            TriCaps(text = stringResource(Res.string.tri_time_common))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(Space.s2), verticalArrangement = Arrangement.spacedBy(Space.s2)) {
                TriTimeSteps.COMMON.forEach { common ->
                    TriChip(text = TriTimeSteps.format(common), selected = time == common, onClick = { time = common })
                }
            }

            HorizontalDivider(color = colors.line)
            when (part) {
                TimePart.HOURS -> {
                    TriCaps(text = stringResource(Res.string.tri_time_hours))
                    NumberGrid(
                        values = TriTimeSteps.HOURS,
                        columns = 4,
                        selected = time.hour,
                        description = { stringResource(Res.string.tri_time_hour_value, it) },
                        onSelect = {
                            time = TriTimeSteps.withHour(time, it)
                            part = TimePart.MINUTES
                        }
                    )
                }
                TimePart.MINUTES -> {
                    TriCaps(text = stringResource(Res.string.tri_time_minutes))
                    NumberGrid(
                        values = TriTimeSteps.MINUTES,
                        columns = 4,
                        selected = time.minute,
                        description = { stringResource(Res.string.tri_time_minute_value, it) },
                        onSelect = { time = TriTimeSteps.withMinute(time, it) }
                    )
                }
            }

            Spacer(modifier = Modifier.heightIn(min = Space.s1))
            TriButton(
                text = stringResource(Res.string.tri_time_use, formatted),
                onClick = { onConfirm(time) },
                variant = TriButtonVariant.Volt,
                large = true,
                modifier = Modifier.fillMaxWidth()
            )
            TriTextButton(
                text = stringResource(Res.string.tri_time_cancel),
                onClick = onDismiss,
                color = colors.inkMuted,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

/** Hours or minutes of the big time: a tab that opens its grid. */
@Composable
private fun TimeSegment(value: String, description: String, selected: Boolean, onClick: () -> Unit) {
    val colors = Triangulacion.colors
    val shape = RoundedCornerShape(Radius.md)
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .heightIn(min = 72.dp)
            .background(if (selected) colors.volt else colors.surface200, shape)
            .border(1.5.dp, if (selected) colors.volt else colors.lineStrong, shape)
            .clickable(role = Role.Tab, onClick = onClick, onClickLabel = description)
            .semantics(mergeDescendants = true) {
                contentDescription = "$description $value"
                this.selected = selected
            }
            .padding(horizontal = Space.s3, vertical = Space.s1)
    ) {
        Text(text = value, style = Triangulacion.type.statXl, color = if (selected) colors.onVolt else colors.ink)
    }
}

/** A grid of radio cells with a 48 dp minimum; the selected one in ink. */
@Composable
private fun NumberGrid(
    values: List<Int>,
    columns: Int,
    selected: Int,
    description: @Composable (Int) -> String,
    onSelect: (Int) -> Unit
) {
    val colors = Triangulacion.colors
    val shape = RoundedCornerShape(Radius.sm)
    Column(verticalArrangement = Arrangement.spacedBy(Space.s2), modifier = Modifier.fillMaxWidth()) {
        values.chunked(columns).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(Space.s2), modifier = Modifier.fillMaxWidth()) {
                row.forEach { value ->
                    val isSelected = value == selected
                    val cellDescription = description(value)
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .background(if (isSelected) colors.ink else colors.surface200, shape)
                            .border(1.5.dp, if (isSelected) colors.ink else colors.lineStrong, shape)
                            .clickable(role = Role.RadioButton, onClick = { onSelect(value) })
                            .semantics(mergeDescendants = true) {
                                contentDescription = cellDescription
                                this.selected = isSelected
                            }
                            .padding(vertical = Space.s2)
                    ) {
                        Text(
                            text = value.twoDigits(),
                            style = Triangulacion.type.title,
                            color = if (isSelected) colors.surface100 else colors.ink,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                // Keeps the cells of a short last row the same width
                repeat(columns - row.size) { Spacer(modifier = Modifier.weight(1f)) }
            }
        }
    }
}
