package com.kikepb.club.presentation.mapper

import com.kikepb.club.domain.model.MatchFormat
import com.kikepb.club.domain.policy.ScheduleValidationError
import com.kikepb.core.presentation.util.UiText
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime
import org.jetbrains.compose.resources.StringResource
import squadfy_app.feature.club.presentation.generated.resources.Res
import squadfy_app.feature.club.presentation.generated.resources.day_friday
import squadfy_app.feature.club.presentation.generated.resources.day_monday
import squadfy_app.feature.club.presentation.generated.resources.day_saturday
import squadfy_app.feature.club.presentation.generated.resources.day_sunday
import squadfy_app.feature.club.presentation.generated.resources.day_thursday
import squadfy_app.feature.club.presentation.generated.resources.day_tuesday
import squadfy_app.feature.club.presentation.generated.resources.day_wednesday
import squadfy_app.feature.club.presentation.generated.resources.format_eleven
import squadfy_app.feature.club.presentation.generated.resources.format_five
import squadfy_app.feature.club.presentation.generated.resources.format_seven
import squadfy_app.feature.club.presentation.generated.resources.format_short_eleven
import squadfy_app.feature.club.presentation.generated.resources.format_short_five
import squadfy_app.feature.club.presentation.generated.resources.format_short_seven
import squadfy_app.feature.club.presentation.generated.resources.schedule_error_close_kickoff
import squadfy_app.feature.club.presentation.generated.resources.schedule_error_days_before
import squadfy_app.feature.club.presentation.generated.resources.schedule_error_draw_close
import squadfy_app.feature.club.presentation.generated.resources.schedule_error_draw_kickoff
import squadfy_app.feature.club.presentation.generated.resources.schedule_error_duration

val DayOfWeek.label: StringResource
    get() = when (this) {
        DayOfWeek.MONDAY -> Res.string.day_monday
        DayOfWeek.TUESDAY -> Res.string.day_tuesday
        DayOfWeek.WEDNESDAY -> Res.string.day_wednesday
        DayOfWeek.THURSDAY -> Res.string.day_thursday
        DayOfWeek.FRIDAY -> Res.string.day_friday
        DayOfWeek.SATURDAY -> Res.string.day_saturday
        DayOfWeek.SUNDAY -> Res.string.day_sunday
    }

val MatchFormat.label: StringResource
    get() = when (this) {
        MatchFormat.FIVE_A_SIDE -> Res.string.format_five
        MatchFormat.SEVEN_A_SIDE -> Res.string.format_seven
        MatchFormat.ELEVEN_A_SIDE -> Res.string.format_eleven
    }

val MatchFormat.shortLabel: StringResource
    get() = when (this) {
        MatchFormat.FIVE_A_SIDE -> Res.string.format_short_five
        MatchFormat.SEVEN_A_SIDE -> Res.string.format_short_seven
        MatchFormat.ELEVEN_A_SIDE -> Res.string.format_short_eleven
    }

fun ScheduleValidationError.toUiText(): UiText = UiText.Resource(
    when (this) {
        ScheduleValidationError.INVALID_DURATION -> Res.string.schedule_error_duration
        ScheduleValidationError.INVALID_DAYS_BEFORE -> Res.string.schedule_error_days_before
        ScheduleValidationError.CLOSE_NOT_BEFORE_KICKOFF -> Res.string.schedule_error_close_kickoff
        ScheduleValidationError.DRAW_BEFORE_CLOSE -> Res.string.schedule_error_draw_close
        ScheduleValidationError.DRAW_NOT_BEFORE_KICKOFF -> Res.string.schedule_error_draw_kickoff
    }
)

/** 24h `HH:mm`, the format used across the club screens. */
fun LocalTime.formatted(): String = "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"
