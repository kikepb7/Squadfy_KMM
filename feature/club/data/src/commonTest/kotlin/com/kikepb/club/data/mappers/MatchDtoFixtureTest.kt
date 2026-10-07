package com.kikepb.club.data.mappers

import com.kikepb.club.data.dto.MatchDTO
import com.kikepb.club.data.dto.TeamBalanceDTO
import com.kikepb.club.domain.model.MatchEventType
import com.kikepb.club.domain.model.MatchStatus
import com.kikepb.club.domain.model.PlayerPosition
import com.kikepb.club.domain.model.Team
import com.kikepb.core.data.networking.squadfyJson
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MatchDtoFixtureTest {

    // MatchDto as the backend serializes it (BACKEND.md §9 + BE-008 fields)
    private val matchV1 = """
        {"id":"d38f9fd7","clubId":"c9080e51","scheduledAt":"2026-10-08T18:00:00Z","status":"SCHEDULED",
         "enrolledPlayers":["m-1","m-2","m-3"],"teamA":["m-1","m-2"],"teamB":["m-3"],"durationMinutes":90,
         "minutesPlayed":{"m-1":90,"m-2":45,"m-3":90},
         "enrolledGuests":[{"guestId":"g-1","name":"Luis","position":"GOALKEEPER","invitedByMemberId":"m-1"}],
         "teamAGuests":[],"teamBGuests":[{"guestId":"g-1","name":"Luis","position":"GOALKEEPER","invitedByMemberId":"m-1"}],
         "isManualScore":false,"ratingChanges":{},"scheduleDate":"2026-10-08","teamAScore":1,"teamBScore":0,
         "goals":[{"id":"ev-2","matchId":"d38f9fd7","clubMemberId":"m-1","type":"GOAL","minute":30,"createdAt":"2026-10-08T18:40:00.123456Z"}],
         "assists":[{"id":"ev-3","matchId":"d38f9fd7","clubMemberId":"m-2","type":"ASSIST","minute":30,"createdAt":"2026-10-08T18:40:01Z"}],
         "yellowCards":[{"id":"ev-1","matchId":"d38f9fd7","clubMemberId":"m-3","type":"YELLOW_CARD","minute":12,"createdAt":"2026-10-08T18:20:00Z"}],
         "redCards":[],"createdAt":"2026-10-01T00:00:00Z","updatedAt":"2026-10-08T18:40:01Z"}
    """.trimIndent()

    private val balanceV1 = """
        {"matchId":"d38f9fd7",
         "teamA":{"players":2,"averageRating":1050,"totalRating":2100,
                  "playerRatings":[{"clubMemberId":"m-2","rating":1000,"isGuest":false},{"clubMemberId":"m-1","rating":1100,"isGuest":false}]},
         "teamB":{"players":2,"averageRating":1000,"totalRating":2000,
                  "playerRatings":[{"clubMemberId":"m-3","rating":1000,"isGuest":false},{"clubMemberId":"g-1","rating":1000,"isGuest":true}]},
         "averageRatingDifference":50,"teamAExpectedScore":0.57}
    """.trimIndent()

    @Test
    fun `AC-006-07 match decodes with teams, minutes and chronological events`() {
        val match = squadfyJson.decodeFromString<MatchDTO>(matchV1).toDomain()

        assertEquals(MatchStatus.SCHEDULED, match.status)
        assertEquals(Team.A, match.teamOf("m-2"))
        assertEquals(Team.B, match.teamOf("m-3"))
        assertNull(match.teamOf("m-9"))
        assertEquals(45, match.minutesOf("m-2"))
        assertEquals(listOf("ev-1", "ev-2", "ev-3"), match.events.map { it.id })
        assertEquals(MatchEventType.YELLOW_CARD, match.events.first().type)
        assertEquals("2026-10-08", match.scheduleDate.toString())
        assertFalse(match.isManualScore)
    }

    @Test
    fun `AC-006-08 guests decode in their team with position`() {
        val match = squadfyJson.decodeFromString<MatchDTO>(matchV1).toDomain()

        assertTrue(match.teamAGuests.isEmpty())
        val guest = match.teamBGuests.single()
        assertEquals("Luis", guest.name)
        assertEquals(PlayerPosition.GOALKEEPER, guest.position)
        assertEquals("m-1", guest.invitedByMemberId)
        assertTrue(match.hasTeams)
    }

    @Test
    fun `AC-006-03 team balance decodes sorted by rating with guests flagged`() {
        val balance = squadfyJson.decodeFromString<TeamBalanceDTO>(balanceV1).toDomain()

        assertEquals(listOf("m-1", "m-2"), balance.teamA.playerRatings.map { it.id })
        assertEquals(1050, balance.teamA.averageRating)
        assertTrue(balance.teamB.playerRatings.single { it.id == "g-1" }.isGuest)
        assertEquals(0.57, balance.teamAExpectedScore)
    }

    @Test
    fun `AC-006-07 minimal match uses defaults and unknown status falls back`() {
        val match = squadfyJson.decodeFromString<MatchDTO>(
            """{"id":"x","clubId":"c","scheduledAt":"2026-10-08T18:00:00Z","status":"POSTPONED","scheduleDate":null}"""
        ).toDomain()

        assertEquals(MatchStatus.UNKNOWN, match.status)
        assertFalse(match.hasTeams)
        assertEquals(60, match.minutesOf("anyone"))
        assertNull(match.scheduleDate)
    }
}
