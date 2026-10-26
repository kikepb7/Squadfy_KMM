package com.kikepb.club.data.mappers

import com.kikepb.club.data.dto.ClubMatchScheduleDTO
import com.kikepb.club.data.dto.ScheduleExceptionDTO
import com.kikepb.club.data.dto.ScheduleRequestDTO
import com.kikepb.club.domain.model.ClubScheduleModel
import com.kikepb.club.domain.model.MatchFormat
import com.kikepb.club.domain.model.ScheduleDraft
import com.kikepb.club.domain.model.ScheduleExceptionModel
import com.kikepb.club.domain.model.ScheduleExceptionType
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlin.time.Instant

fun ClubMatchScheduleDTO.toDomain(): ClubScheduleModel = ClubScheduleModel(
    id = id,
    clubId = clubId,
    dayOfWeek = DayOfWeek.valueOf(matchDayOfWeek),
    matchTime = LocalTime.parse(matchTime),
    timeZone = timeZone,
    format = MatchFormat.fromRaw(format),
    maxPlayers = maxPlayers,
    durationMinutes = matchDurationMinutes,
    closeDaysBefore = closeDaysBefore,
    closeTime = LocalTime.parse(closeTime),
    drawDaysBefore = drawDaysBefore,
    drawTime = LocalTime.parse(drawTime),
    isActive = isActive
)

/** Close and draw times are only sent when the club uses custom deadlines (flag `CUSTOM_DRAW_TIME`). */
fun ScheduleDraft.toRequest(includeDeadlines: Boolean): ScheduleRequestDTO = ScheduleRequestDTO(
    matchDayOfWeek = dayOfWeek.name,
    matchTime = matchTime.toBackendTime(),
    timeZone = timeZone,
    format = format.name,
    matchDurationMinutes = durationMinutes,
    isActive = isActive,
    closeDaysBefore = closeDaysBefore.takeIf { includeDeadlines },
    closeTime = closeTime.toBackendTime().takeIf { includeDeadlines },
    drawDaysBefore = drawDaysBefore.takeIf { includeDeadlines },
    drawTime = drawTime.toBackendTime().takeIf { includeDeadlines }
)

fun ScheduleExceptionDTO.toDomain(): ScheduleExceptionModel = ScheduleExceptionModel(
    id = id,
    clubId = clubId,
    date = LocalDate.parse(date),
    type = ScheduleExceptionType.entries.firstOrNull { it.name == type } ?: ScheduleExceptionType.CANCELLED,
    newScheduledAt = newScheduledAt?.let(Instant::parse),
    reason = reason
)

/** The backend accepts and returns `HH:mm:ss`. */
fun LocalTime.toBackendTime(): String =
    "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}:00"
