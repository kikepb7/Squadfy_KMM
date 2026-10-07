package com.kikepb.club.data.mappers

import com.kikepb.club.data.dto.MemberAbsenceDTO
import com.kikepb.core.data.networking.squadfyJson
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AbsenceDtoFixtureTest {

    @Test
    fun `AC-014-01 absences decode with local dates and optional reason`() {
        val absences = squadfyJson.decodeFromString<List<MemberAbsenceDTO>>(
            """[{"id":"ab-1","clubId":"c","clubMemberId":"m-1","fromDate":"2026-10-10","toDate":"2026-10-20","reason":"Vacaciones","createdAt":"2026-10-07T10:00:00.123456Z"},
                {"id":"ab-2","clubId":"c","clubMemberId":"m-2","fromDate":"2026-11-01","toDate":"2026-11-01","reason":null,"createdAt":"2026-10-07T10:00:00Z"}]"""
        ).map { it.toDomain() }

        assertEquals(LocalDate(2026, 10, 10), absences[0].fromDate)
        assertEquals("Vacaciones", absences[0].reason)
        assertNull(absences[1].reason)
    }
}
