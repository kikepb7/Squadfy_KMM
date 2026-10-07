package com.kikepb.club.domain.policy

import com.kikepb.club.domain.model.AnnouncementStatus
import com.kikepb.club.domain.model.MatchAnnouncementModel
import com.kikepb.club.domain.model.MatchGuestModel
import com.kikepb.club.domain.model.MatchModel
import com.kikepb.club.domain.model.MatchStatus
import com.kikepb.club.domain.model.Team
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Instant

class MatchTeamsPolicyTest {

    private val closesAt = Instant.parse("2026-10-07T20:00:00Z")
    private val drawAt = Instant.parse("2026-10-08T10:00:00Z")
    private val guest = MatchGuestModel(guestId = "g-1", name = "Luis", position = null, invitedByMemberId = "m-1")

    @Test
    fun `AC-006-02 open announcement without teams publishes them at close`() {
        val pending = MatchTeamsPolicy.pending(match(), announcement(AnnouncementStatus.OPEN), now = Instant.parse("2026-10-07T10:00:00Z"))
        assertEquals(TeamsPending.AtClose(closesAt), pending)
    }

    @Test
    fun `AC-006-09 closed announcement before the draw time shows the draw time`() {
        val pending = MatchTeamsPolicy.pending(match(), announcement(AnnouncementStatus.CLOSED), now = Instant.parse("2026-10-08T08:00:00Z"))
        assertEquals(TeamsPending.AtDraw(drawAt), pending)
    }

    @Test
    fun `AC-006-02 closed announcement after the draw time shows pending teams`() {
        val pending = MatchTeamsPolicy.pending(match(), announcement(AnnouncementStatus.CLOSED), now = Instant.parse("2026-10-08T11:00:00Z"))
        assertEquals(TeamsPending.Pending, pending)
    }

    @Test
    fun `AC-006-01 a match with teams has nothing pending`() {
        assertNull(MatchTeamsPolicy.pending(match(teamA = listOf("m-1"), teamB = listOf("m-2")), announcement(AnnouncementStatus.CLOSED), drawAt))
    }

    @Test
    fun `AC-006-06 only managers rectify and only while scheduled`() {
        assertTrue(MatchTeamsPolicy.canRectify(match(), isManager = true))
        assertFalse(MatchTeamsPolicy.canRectify(match(), isManager = false))
        assertFalse(MatchTeamsPolicy.canRectify(match(status = MatchStatus.COMPLETED), isManager = true))
        assertFalse(MatchTeamsPolicy.canRectify(match(status = MatchStatus.CANCELLED), isManager = true))
    }

    @Test
    fun `AC-006-08 manual split starts from the teams including guests and moves them`() {
        val split = MatchTeamsPolicy.initialSplit(match(teamA = listOf("m-1", "m-2"), teamB = listOf("m-3"), teamBGuests = listOf(guest)))
        assertEquals(listOf("m-3", "g-1"), split.teamB.map { it.id })
        assertTrue(split.teamB.last().isGuest)
        assertTrue(split.isValid)

        val moved = split.move("g-1")
        assertEquals(Team.A, moved.teamOf("g-1"))
        assertFalse(moved.isValid) // 3 vs 1
    }

    @Test
    fun `AC-006-05 without teams the confirmed participants are split in halves`() {
        val split = MatchTeamsPolicy.initialSplit(match(enrolled = listOf("m-1", "m-2", "m-3"), enrolledGuests = listOf(guest)))
        assertEquals(2, split.teamA.size)
        assertEquals(2, split.teamB.size)
        assertTrue(split.isValid)
    }

    private fun announcement(status: AnnouncementStatus) = MatchAnnouncementModel(
        id = "a-1", matchId = "x", clubId = "c", maxPlayers = 10, confirmedCount = 0, waitlistCount = 0,
        opensAt = Instant.parse("2026-10-02T00:00:00Z"), closesAt = closesAt, drawAt = drawAt,
        status = status, entries = emptyList(), waitlist = emptyList()
    )

    private fun match(
        status: MatchStatus = MatchStatus.SCHEDULED,
        enrolled: List<String> = emptyList(),
        enrolledGuests: List<MatchGuestModel> = emptyList(),
        teamA: List<String> = emptyList(),
        teamB: List<String> = emptyList(),
        teamBGuests: List<MatchGuestModel> = emptyList()
    ) = MatchModel(
        id = "x", clubId = "c", scheduledAt = Instant.parse("2026-10-08T18:00:00Z"), status = status,
        enrolledPlayers = enrolled, enrolledGuests = enrolledGuests, teamA = teamA, teamB = teamB,
        teamAGuests = emptyList(), teamBGuests = teamBGuests, durationMinutes = 60, minutesPlayed = emptyMap(),
        teamAScore = 0, teamBScore = 0, isManualScore = false, events = emptyList(), ratingChanges = emptyMap(), scheduleDate = null
    )
}
