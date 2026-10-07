package com.kikepb.club.domain.policy

import com.kikepb.club.domain.model.MatchFormat
import com.kikepb.club.domain.model.ScheduleDraft
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ScheduleValidationTest {

    private fun draft(
        matchTime: LocalTime = LocalTime(20, 0),
        duration: Int = 60,
        closeDays: Int = 1,
        closeTime: LocalTime = LocalTime(22, 0),
        drawDays: Int = 1,
        drawTime: LocalTime = LocalTime(22, 0)
    ) = ScheduleDraft(
        dayOfWeek = DayOfWeek.THURSDAY, matchTime = matchTime, timeZone = "Europe/Madrid", format = MatchFormat.FIVE_A_SIDE,
        durationMinutes = duration, closeDaysBefore = closeDays, closeTime = closeTime, drawDaysBefore = drawDays,
        drawTime = drawTime, isActive = true
    )

    @Test
    fun `AC-004-01 duration must be between 10 and 180 minutes`() {
        assertEquals(ScheduleValidationError.INVALID_DURATION, ScheduleValidation.validate(draft(duration = 9), customDeadlines = false))
        assertEquals(ScheduleValidationError.INVALID_DURATION, ScheduleValidation.validate(draft(duration = 181), customDeadlines = false))
        assertNull(ScheduleValidation.validate(draft(duration = 180), customDeadlines = false))
    }

    @Test
    fun `AC-004-06 default close and draw (day before at 22h) are valid`() {
        assertNull(ScheduleValidation.validate(draft(), customDeadlines = true))
    }

    @Test
    fun `AC-004-06 BE-008 CA-9 close the day before at 21h and draw on match day at 12h is valid`() {
        assertNull(ScheduleValidation.validate(draft(closeTime = LocalTime(21, 0), drawDays = 0, drawTime = LocalTime(12, 0)), customDeadlines = true))
    }

    @Test
    fun `AC-004-06 draw before close, close after kickoff and draw after kickoff are rejected`() {
        assertEquals(
            ScheduleValidationError.DRAW_BEFORE_CLOSE,
            ScheduleValidation.validate(draft(closeTime = LocalTime(21, 0), drawTime = LocalTime(20, 0)), customDeadlines = true)
        )
        assertEquals(
            ScheduleValidationError.CLOSE_NOT_BEFORE_KICKOFF,
            ScheduleValidation.validate(draft(closeDays = 0, closeTime = LocalTime(20, 30)), customDeadlines = true)
        )
        assertEquals(
            ScheduleValidationError.DRAW_NOT_BEFORE_KICKOFF,
            ScheduleValidation.validate(draft(drawDays = 0, drawTime = LocalTime(20, 0)), customDeadlines = true)
        )
        assertEquals(ScheduleValidationError.INVALID_DAYS_BEFORE, ScheduleValidation.validate(draft(closeDays = 7), customDeadlines = true))
    }

    @Test
    fun `AC-004-06 deadlines are not validated when the custom deadlines feature is off`() {
        assertNull(ScheduleValidation.validate(draft(closeDays = 0, closeTime = LocalTime(23, 0)), customDeadlines = false))
    }

    @Test
    fun `AC-004-08 exception dates must be future match days`() {
        val today = LocalDate(2026, 10, 7) // Wednesday
        assertTrue(ScheduleValidation.isValidExceptionDate(LocalDate(2026, 10, 8), DayOfWeek.THURSDAY, today))
        assertFalse(ScheduleValidation.isValidExceptionDate(LocalDate(2026, 10, 9), DayOfWeek.THURSDAY, today))
        assertFalse(ScheduleValidation.isValidExceptionDate(LocalDate(2026, 10, 1), DayOfWeek.THURSDAY, today))
    }
}
