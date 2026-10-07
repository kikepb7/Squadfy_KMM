package com.kikepb.club.domain.model

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlin.time.Instant

/** BE-002 RN-1: seats are fixed by the format (2 × players per side). */
enum class MatchFormat(val maxPlayers: Int) {
    FIVE_A_SIDE(maxPlayers = 10),
    SEVEN_A_SIDE(maxPlayers = 14),
    ELEVEN_A_SIDE(maxPlayers = 22);

    companion object {
        /** Unknown values fall back to the backend default (11v11), APP-RN-13. */
        fun fromRaw(raw: String?): MatchFormat = entries.firstOrNull { it.name == raw } ?: ELEVEN_A_SIDE
    }
}

/** Weekly schedule of a club (`ClubMatchScheduleDto`, BE-002 + BE-008 RN-D). */
data class ClubScheduleModel(
    val id: String,
    val clubId: String,
    val dayOfWeek: DayOfWeek,
    val matchTime: LocalTime,
    val timeZone: String,
    val format: MatchFormat,
    val maxPlayers: Int,
    val durationMinutes: Int,
    val closeDaysBefore: Int,
    val closeTime: LocalTime,
    val drawDaysBefore: Int,
    val drawTime: LocalTime,
    val isActive: Boolean
)

/** What a manager edits; [ScheduleValidation] checks it before sending (AC-004-01/06). */
data class ScheduleDraft(
    val dayOfWeek: DayOfWeek,
    val matchTime: LocalTime,
    val timeZone: String,
    val format: MatchFormat,
    val durationMinutes: Int,
    val closeDaysBefore: Int,
    val closeTime: LocalTime,
    val drawDaysBefore: Int,
    val drawTime: LocalTime,
    val isActive: Boolean
) {
    companion object {
        val DEFAULT_CLOSE_TIME = LocalTime(hour = 22, minute = 0)
        const val DEFAULT_DURATION_MINUTES = 60

        fun from(schedule: ClubScheduleModel) = ScheduleDraft(
            dayOfWeek = schedule.dayOfWeek,
            matchTime = schedule.matchTime,
            timeZone = schedule.timeZone,
            format = schedule.format,
            durationMinutes = schedule.durationMinutes,
            closeDaysBefore = schedule.closeDaysBefore,
            closeTime = schedule.closeTime,
            drawDaysBefore = schedule.drawDaysBefore,
            drawTime = schedule.drawTime,
            isActive = schedule.isActive
        )
    }
}

enum class ScheduleExceptionType { CANCELLED, RESCHEDULED }

/** One week of the schedule cancelled or moved (BE-008 RN-B). */
data class ScheduleExceptionModel(
    val id: String,
    val clubId: String,
    val date: LocalDate,
    val type: ScheduleExceptionType,
    val newScheduledAt: Instant?,
    val reason: String?
)
