package com.kikepb.club.data.mappers

import com.kikepb.club.data.dto.PlayerRatingDTO
import com.kikepb.club.data.dto.PlayerStatsDTO
import com.kikepb.club.data.dto.RatingLeaderboardEntryDTO
import com.kikepb.club.domain.usecase.noCompletedMatches
import com.kikepb.core.data.networking.squadfyJson
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class StandingsDtoFixtureTest {

    @Test
    fun `AC-008-02 rating leaderboard decodes with shared ranks and provisional flag`() {
        val rows = squadfyJson.decodeFromString<List<RatingLeaderboardEntryDTO>>(
            """[{"rank":1,"clubMemberId":"m-1","rating":1032,"matchesRated":6,"isProvisional":false},
                {"rank":1,"clubMemberId":"m-2","rating":1032,"matchesRated":2,"isProvisional":true},
                {"rank":3,"clubMemberId":"m-3","rating":1000,"matchesRated":0,"isProvisional":true}]"""
        ).map { it.toDomain() }

        assertEquals(listOf(1, 1, 3), rows.map { it.rank })
        assertTrue(rows[1].isProvisional)
        assertFalse(rows.noCompletedMatches())
    }

    @Test
    fun `AC-008-02 my rating decodes rank and total`() {
        val mine = squadfyJson.decodeFromString<PlayerRatingDTO>(
            """{"clubId":"c","clubMemberId":"m-2","rating":1032,"matchesRated":2,"isProvisional":true,"rank":1,"totalPlayers":3}"""
        ).toDomain()

        assertEquals(1, mine.rank)
        assertEquals(3, mine.totalPlayers)
    }

    @Test
    fun `AC-008-03 stats leaderboard keeps members with zeros`() {
        val rows = squadfyJson.decodeFromString<List<PlayerStatsDTO>>(
            """[{"rank":1,"clubMemberId":"m-1","matchesPlayed":3,"wins":2,"draws":0,"losses":1,"goals":4,"assists":1,"yellowCards":1,"redCards":0,"minutesPlayed":180},
                {"rank":2,"clubMemberId":"m-2","matchesPlayed":0,"wins":0,"draws":0,"losses":0,"goals":0,"assists":0,"yellowCards":0,"redCards":0,"minutesPlayed":0}]"""
        ).toStatsEntries()

        assertEquals(4, rows[0].stats.goals)
        assertEquals(0, rows[1].stats.matchesPlayed)
        assertEquals(2, rows[1].rank)
    }
}
