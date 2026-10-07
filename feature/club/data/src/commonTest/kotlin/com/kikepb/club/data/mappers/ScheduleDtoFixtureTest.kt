package com.kikepb.club.data.mappers

import com.kikepb.club.data.dto.ClubMatchScheduleDTO
import com.kikepb.club.data.dto.ScheduleExceptionDTO
import com.kikepb.club.domain.model.MatchFormat
import com.kikepb.club.domain.model.ScheduleDraft
import com.kikepb.club.domain.model.ScheduleExceptionType
import com.kikepb.core.data.networking.squadfyJson
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ScheduleDtoFixtureTest {

    // BACKEND.md §9 example ("POST /api/v1/clubs/{clubId}/schedule → 201") plus the BE-008 deadline fields
    private val scheduleV1 = """
        {"id":"40117700-cc62-45b2-a6d3-50c0b633c6dc","clubId":"c9080e51-2da6-43bb-acb5-c0271b4dd1c2",
         "matchDayOfWeek":"THURSDAY","matchTime":"20:00:00","timeZone":"Europe/Madrid","format":"FIVE_A_SIDE",
         "maxPlayers":10,"matchDurationMinutes":60,"closeDaysBefore":1,"closeTime":"21:00:00","drawDaysBefore":0,
         "drawTime":"12:00:00","isActive":true,"createdAt":"2026-10-07T00:13:51.435223Z","updatedAt":"2026-10-07T00:13:51.435225Z"}
    """.trimIndent()

    @Test
    fun `AC-004-01 v1 schedule decodes with format, time zone and deadlines`() {
        val schedule = squadfyJson.decodeFromString<ClubMatchScheduleDTO>(scheduleV1).toDomain()

        assertEquals(DayOfWeek.THURSDAY, schedule.dayOfWeek)
        assertEquals(LocalTime(20, 0), schedule.matchTime)
        assertEquals(MatchFormat.FIVE_A_SIDE, schedule.format)
        assertEquals(10, schedule.maxPlayers)
        assertEquals(LocalTime(21, 0), schedule.closeTime)
        assertEquals(0, schedule.drawDaysBefore)
    }

    @Test
    fun `AC-004-06 deadlines are only sent with the custom deadlines feature`() {
        val draft = ScheduleDraft.from(squadfyJson.decodeFromString<ClubMatchScheduleDTO>(scheduleV1).toDomain())

        val withDeadlines = draft.toRequest(includeDeadlines = true)
        val withoutDeadlines = draft.toRequest(includeDeadlines = false)

        assertEquals("21:00:00", withDeadlines.closeTime)
        assertEquals("20:00:00", withDeadlines.matchTime)
        assertNull(withoutDeadlines.closeTime)
        assertNull(withoutDeadlines.drawDaysBefore)
    }

    @Test
    fun `AC-004-08 rescheduled exception decodes with its new instant`() {
        val exception = squadfyJson.decodeFromString<ScheduleExceptionDTO>(
            """{"id":"e-1","clubId":"c-1","date":"2026-10-15","type":"RESCHEDULED","newScheduledAt":"2026-10-16T19:00:00Z","reason":"Pista ocupada","createdAt":"2026-10-07T10:00:00Z"}"""
        ).toDomain()

        assertEquals(LocalDate(2026, 10, 15), exception.date)
        assertEquals(ScheduleExceptionType.RESCHEDULED, exception.type)
        assertEquals("2026-10-16T19:00:00Z", exception.newScheduledAt.toString())
    }
}
