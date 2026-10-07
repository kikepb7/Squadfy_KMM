package com.kikepb.club.domain.policy

import com.kikepb.club.domain.model.MatchModel
import com.kikepb.club.domain.model.MatchStatus
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Instant

class MatchCyclePolicyTest {

    private val kickOff = Instant.parse("2026-10-15T18:00:00Z")

    @Test
    fun `AC-007-05 completing needs a started scheduled match with teams`() {
        val withTeams = match(teamA = listOf("a"), teamB = listOf("b"))
        assertTrue(MatchCyclePolicy.canComplete(withTeams, isManager = true, now = kickOff))
        assertFalse(MatchCyclePolicy.canComplete(withTeams, isManager = true, now = Instant.parse("2026-10-15T17:59:00Z")))
        assertFalse(MatchCyclePolicy.canComplete(match(), isManager = true, now = kickOff))
        assertFalse(MatchCyclePolicy.canComplete(withTeams, isManager = false, now = kickOff))
    }

    @Test
    fun `AC-007-06 only the latest completed match can be reopened`() {
        val completed = match(status = MatchStatus.COMPLETED)
        assertTrue(MatchCyclePolicy.canReopen(completed, isManager = true, latestCompletedId = "x"))
        assertFalse(MatchCyclePolicy.canReopen(completed, isManager = true, latestCompletedId = "newer"))
        assertFalse(MatchCyclePolicy.canReopen(match(), isManager = true, latestCompletedId = "x"))
    }

    @Test
    fun `AC-007-07 completed or cancelled matches cannot be cancelled`() {
        assertTrue(MatchCyclePolicy.canCancel(match(), isManager = true))
        assertFalse(MatchCyclePolicy.canCancel(match(status = MatchStatus.COMPLETED), isManager = true))
        assertFalse(MatchCyclePolicy.canCancel(match(status = MatchStatus.CANCELLED), isManager = true))
        assertFalse(MatchCyclePolicy.canCancel(match(), isManager = false))
    }

    @Test
    fun `AC-007-09 a completed match is read-only`() {
        assertFalse(MatchCyclePolicy.canRecord(match(status = MatchStatus.COMPLETED, teamA = listOf("a"), teamB = listOf("b")), isManager = true))
    }

    @Test
    fun `AC-007-04 minutes go from 0 to the duration`() {
        val match = match()
        assertTrue(MatchCyclePolicy.isValidMinutes(match, 0))
        assertTrue(MatchCyclePolicy.isValidMinutes(match, 60))
        assertFalse(MatchCyclePolicy.isValidMinutes(match, 61))
        assertFalse(MatchCyclePolicy.isValidMinutes(match, -1))
        assertFalse(MatchCyclePolicy.isValidMinutes(match, null))
    }

    private fun match(status: MatchStatus = MatchStatus.SCHEDULED, teamA: List<String> = emptyList(), teamB: List<String> = emptyList()) = MatchModel(
        id = "x", clubId = "c", scheduledAt = kickOff, status = status, enrolledPlayers = teamA + teamB, enrolledGuests = emptyList(),
        teamA = teamA, teamB = teamB, teamAGuests = emptyList(), teamBGuests = emptyList(), durationMinutes = 60, minutesPlayed = emptyMap(),
        teamAScore = 0, teamBScore = 0, isManualScore = false, events = emptyList(), ratingChanges = emptyMap(), scheduleDate = null
    )
}
