package com.kikepb.globalPosition.presentation.home

import com.kikepb.club.domain.model.MatchModel
import com.kikepb.club.domain.model.MatchStatus
import com.kikepb.core.designsystem.triangulacion.components.MatchResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Instant

class HomeLastMatchdayTest {

    private fun match(
        id: String,
        day: Int,
        teamA: List<String>,
        teamB: List<String>,
        scoreA: Int,
        scoreB: Int,
        status: MatchStatus = MatchStatus.COMPLETED,
        ratingChanges: Map<String, Int> = emptyMap()
    ) = MatchModel(
        id = id, clubId = "club-1", scheduledAt = Instant.parse("2026-10-${day.toString().padStart(2, '0')}T19:00:00Z"),
        status = status, enrolledPlayers = teamA + teamB, enrolledGuests = emptyList(), teamA = teamA, teamB = teamB,
        teamAGuests = emptyList(), teamBGuests = emptyList(), durationMinutes = 60, minutesPlayed = emptyMap(),
        teamAScore = scoreA, teamBScore = scoreB, isManualScore = false, events = emptyList(), ratingChanges = ratingChanges,
        scheduleDate = null
    )

    @Test
    fun `AC-018-06 the last matchday is my latest completed match, seen from my team`() {
        val matches = listOf(
            match("m1", 1, teamA = listOf("me"), teamB = listOf("x"), scoreA = 2, scoreB = 2),
            match("m2", 8, teamA = listOf("x"), teamB = listOf("me"), scoreA = 3, scoreB = 4, ratingChanges = mapOf("me" to 12)),
            match("m3", 9, teamA = listOf("x"), teamB = listOf("y"), scoreA = 1, scoreB = 0),
            match("m4", 15, teamA = listOf("me"), teamB = listOf("x"), scoreA = 0, scoreB = 0, status = MatchStatus.SCHEDULED)
        )

        val last = HomeLastMatchdayPolicy.of("club-1", "Albaicín FC", "me", matches, "Europe/Madrid")!!

        assertEquals("m2", last.matchId)
        assertEquals(4, last.myScore)
        assertEquals(3, last.theirScore)
        assertEquals(MatchResult.WIN, last.result)
        assertEquals(12, last.ratingChange)
        assertEquals(listOf(MatchResult.DRAW, MatchResult.WIN), last.form)
    }

    @Test
    fun `AC-018-06 without a completed match of mine there is no last matchday`() {
        val matches = listOf(match("m1", 1, teamA = listOf("x"), teamB = listOf("y"), scoreA = 1, scoreB = 0))
        assertNull(HomeLastMatchdayPolicy.of("club-1", "Club", "me", matches, null))
    }

    @Test
    fun `AC-018-06 the form keeps the last five, oldest first`() {
        val matches = (1..7).map { day -> match("m$day", day, listOf("me"), listOf("x"), scoreA = if (day % 2 == 0) 1 else 0, scoreB = 0) }
        val last = HomeLastMatchdayPolicy.of("club-1", "Club", "me", matches, null)!!
        assertEquals(
            listOf(MatchResult.DRAW, MatchResult.WIN, MatchResult.DRAW, MatchResult.WIN, MatchResult.DRAW),
            last.form
        )
    }
}
