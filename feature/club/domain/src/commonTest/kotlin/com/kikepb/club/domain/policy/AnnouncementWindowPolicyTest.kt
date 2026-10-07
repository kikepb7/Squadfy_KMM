package com.kikepb.club.domain.policy

import com.kikepb.club.domain.model.AnnouncementEntry
import com.kikepb.club.domain.model.AnnouncementStatus
import com.kikepb.club.domain.model.CurrentAnnouncementModel
import com.kikepb.club.domain.model.EntryStatus
import com.kikepb.club.domain.model.MatchAnnouncementModel
import com.kikepb.club.domain.model.MyEnrollmentStatus
import com.kikepb.club.domain.model.ParticipantType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

class AnnouncementWindowPolicyTest {

    // Spec 005 example table: Thursday 2026-10-15 20:00 Europe/Madrid (BE-002 CA-1)
    private fun announcement(
        status: AnnouncementStatus = AnnouncementStatus.OPEN,
        opensAt: String = "2026-10-08T22:00:00Z",
        closesAt: String = "2026-10-14T20:00:00Z",
        entries: List<AnnouncementEntry> = emptyList(),
        waitlist: List<AnnouncementEntry> = emptyList()
    ) = MatchAnnouncementModel(
        id = "a-1", matchId = "m-1", clubId = "c-1", maxPlayers = 10, confirmedCount = entries.size, waitlistCount = waitlist.size,
        opensAt = Instant.parse(opensAt), closesAt = Instant.parse(closesAt), drawAt = Instant.parse(closesAt),
        status = status, entries = entries, waitlist = waitlist
    )

    private fun entry(id: String, memberId: String?, status: EntryStatus = EntryStatus.CONFIRMED, invitedBy: String? = null) = AnnouncementEntry(
        id = id,
        participantType = if (memberId == null) ParticipantType.GUEST else ParticipantType.MEMBER,
        clubMemberId = memberId, guestName = if (memberId == null) "Guest $id" else null, guestPosition = null,
        invitedByMemberId = invitedBy, status = status, enrolledAt = Instant.parse("2026-10-09T10:00:00Z")
    )

    @Test
    fun `AC-005-03 window state follows the normative example table`() {
        val a = announcement()
        assertEquals(WindowState.NOT_OPEN, AnnouncementWindowPolicy.state(a, Instant.parse("2026-10-08T21:59:00Z")))
        assertEquals(WindowState.OPEN, AnnouncementWindowPolicy.state(a, Instant.parse("2026-10-08T22:00:00Z")))
        assertEquals(WindowState.OPEN, AnnouncementWindowPolicy.state(a, Instant.parse("2026-10-14T19:59:00Z")))
        assertEquals(WindowState.CLOSED, AnnouncementWindowPolicy.state(a, Instant.parse("2026-10-14T20:00:00Z")))
        assertEquals(WindowState.CANCELLED, AnnouncementWindowPolicy.state(announcement(status = AnnouncementStatus.CANCELLED), Instant.parse("2026-10-10T10:00:00Z")))
    }

    @Test
    fun `AC-005-03 DST week closes at 22h CET (21h UTC)`() {
        val a = announcement(opensAt = "2026-10-22T22:00:00Z", closesAt = "2026-10-28T21:00:00Z")
        assertEquals(WindowState.OPEN, AnnouncementWindowPolicy.state(a, Instant.parse("2026-10-28T20:59:00Z")))
        assertEquals(WindowState.CLOSED, AnnouncementWindowPolicy.state(a, Instant.parse("2026-10-28T21:00:00Z")))
    }

    @Test
    fun `AC-005-03 countdown targets the next boundary`() {
        val a = announcement()
        assertEquals(1.minutes, AnnouncementWindowPolicy.timeToNextChange(a, Instant.parse("2026-10-14T19:59:00Z")))
        assertEquals(1.minutes, AnnouncementWindowPolicy.timeToNextChange(a, Instant.parse("2026-10-08T21:59:00Z")))
        assertEquals(null, AnnouncementWindowPolicy.timeToNextChange(a, Instant.parse("2026-10-14T20:00:00Z")))
    }

    @Test
    fun `AC-005-05 my status is derived from the returned announcement`() {
        val current = CurrentAnnouncementModel(announcement(), Instant.parse("2026-10-15T18:00:00Z"), MyEnrollmentStatus.NOT_ENROLLED, null)
        val full = announcement(
            entries = listOf(entry("e1", "m-other")),
            waitlist = listOf(entry("e2", "m-x", EntryStatus.WAITLISTED), entry("e3", "me", EntryStatus.WAITLISTED))
        )

        val updated = AnnouncementWindowPolicy.withMyStatus(current, full, myMemberId = "me")

        assertEquals(MyEnrollmentStatus.WAITLISTED, updated.myStatus)
        assertEquals(2, updated.myWaitlistPosition)
    }

    @Test
    fun `AC-005-13 a guest I invited never counts as me and counts towards my 2 guests`() {
        val a = announcement(entries = listOf(entry("g1", memberId = null, invitedBy = "me"), entry("g2", memberId = null, invitedBy = "other")))
        val current = CurrentAnnouncementModel(a, Instant.parse("2026-10-15T18:00:00Z"), MyEnrollmentStatus.NOT_ENROLLED, null)

        assertEquals(MyEnrollmentStatus.NOT_ENROLLED, AnnouncementWindowPolicy.withMyStatus(current, a, "me").myStatus)
        assertEquals(1, AnnouncementWindowPolicy.myGuestsCount(a, "me"))
    }
}
