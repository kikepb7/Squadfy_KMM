package com.kikepb.club.data.mappers

import com.kikepb.club.data.dto.CurrentMatchAnnouncementDTO
import com.kikepb.club.domain.model.AnnouncementStatus
import com.kikepb.club.domain.model.EntryStatus
import com.kikepb.club.domain.model.MyEnrollmentStatus
import com.kikepb.club.domain.model.ParticipantType
import com.kikepb.club.domain.model.PlayerPosition
import com.kikepb.core.data.networking.squadfyJson
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AnnouncementDtoFixtureTest {

    // BACKEND.md §9 example of GET /announcements/current, plus a guest entry and drawAt (BE-008)
    private val currentV1 = """
        {"announcement":{"id":"90dd41a6-b0e5-4243-a735-c79d7226ede2","matchId":"d38f9fd7-447e-4e10-bfd4-395bfbead889",
          "clubId":"c9080e51-2da6-43bb-acb5-c0271b4dd1c2","maxPlayers":10,"confirmedCount":2,"waitlistCount":0,
          "opensAt":"2026-10-07T00:13:51.446611Z","closesAt":"2026-10-07T20:00:00Z","drawAt":"2026-10-08T10:00:00Z","status":"OPEN",
          "entries":[
            {"id":"e-1","matchAnnouncementId":"90dd41a6","participantType":"MEMBER","clubMemberId":"m-1","guestName":null,
             "guestPosition":null,"invitedByMemberId":null,"status":"CONFIRMED","enrolledAt":"2026-10-07T08:00:00.123456Z"},
            {"id":"g-1","matchAnnouncementId":"90dd41a6","participantType":"GUEST","clubMemberId":null,"guestName":"Luis",
             "guestPosition":"GOALKEEPER","invitedByMemberId":"m-1","status":"CONFIRMED","enrolledAt":"2026-10-07T08:05:00Z"}
          ],"waitlist":[],"createdAt":"2026-10-07T00:13:51.447442Z","updatedAt":"2026-10-07T00:13:51.447444Z"},
         "matchScheduledAt":"2026-10-08T18:00:00Z","myStatus":"NOT_ENROLLED","myWaitlistPosition":null}
    """.trimIndent()

    @Test
    fun `AC-005-02 current announcement decodes with entries, guests and draw time`() {
        val current = squadfyJson.decodeFromString<CurrentMatchAnnouncementDTO>(currentV1).toDomain()

        assertEquals(MyEnrollmentStatus.NOT_ENROLLED, current.myStatus)
        assertNull(current.myWaitlistPosition)
        assertEquals(AnnouncementStatus.OPEN, current.announcement.status)
        assertEquals("2026-10-08T10:00:00Z", current.announcement.drawAt.toString())
        val guest = current.announcement.entries[1]
        assertEquals(ParticipantType.GUEST, guest.participantType)
        assertEquals("Luis", guest.guestName)
        assertEquals(PlayerPosition.GOALKEEPER, guest.guestPosition)
        assertEquals("m-1", guest.invitedByMemberId)
        assertEquals(EntryStatus.CONFIRMED, guest.status)
    }

    @Test
    fun `AC-005-14 drawAt falls back to closesAt for announcements without it`() {
        val legacy = currentV1.replace("\"drawAt\":\"2026-10-08T10:00:00Z\",", "")
        val announcement = squadfyJson.decodeFromString<CurrentMatchAnnouncementDTO>(legacy).toDomain().announcement

        assertEquals(announcement.closesAt, announcement.drawAt)
    }
}
