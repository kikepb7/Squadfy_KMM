package com.kikepb.club.domain.policy

import com.kikepb.club.domain.model.ScheduleDraft
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

enum class ScheduleValidationError {
    INVALID_DURATION,
    INVALID_DAYS_BEFORE,
    CLOSE_NOT_BEFORE_KICKOFF,
    DRAW_BEFORE_CLOSE,
    DRAW_NOT_BEFORE_KICKOFF
}

/**
 * Local mirror of the backend schedule validation (BE-002 RN-1, BE-008 RN-D2). The server stays
 * authoritative (400); this only avoids obviously invalid requests.
 */
object ScheduleValidation {

    val DURATION_RANGE = 10..180
    val DAYS_BEFORE_RANGE = 0..6

    fun validate(draft: ScheduleDraft, customDeadlines: Boolean): ScheduleValidationError? {
        if (draft.durationMinutes !in DURATION_RANGE) return ScheduleValidationError.INVALID_DURATION
        if (!customDeadlines) return null
        if (draft.closeDaysBefore !in DAYS_BEFORE_RANGE || draft.drawDaysBefore !in DAYS_BEFORE_RANGE) {
            return ScheduleValidationError.INVALID_DAYS_BEFORE
        }
        // Minutes relative to kickoff (negative = before the match), ignoring DST: deadlines are at most 6 days away
        val kickoff = draft.matchTime.toMinutes()
        val close = -draft.closeDaysBefore * MINUTES_PER_DAY + draft.closeTime.toMinutes() - kickoff
        val draw = -draft.drawDaysBefore * MINUTES_PER_DAY + draft.drawTime.toMinutes() - kickoff
        return when {
            close >= 0 -> ScheduleValidationError.CLOSE_NOT_BEFORE_KICKOFF
            draw < close -> ScheduleValidationError.DRAW_BEFORE_CLOSE
            draw >= 0 -> ScheduleValidationError.DRAW_NOT_BEFORE_KICKOFF
            else -> null
        }
    }

    /** BE-008 RN-B1: an exception date must be a future match day of the schedule. */
    fun isValidExceptionDate(date: LocalDate, matchDay: DayOfWeek, today: LocalDate): Boolean =
        date.dayOfWeek == matchDay && date > today

    private fun LocalTime.toMinutes() = hour * 60 + minute

    private const val MINUTES_PER_DAY = 24 * 60
}
