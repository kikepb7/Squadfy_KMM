package com.kikepb.club.presentation.memberdetail

import com.kikepb.club.domain.model.MatchRatingChange
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Instant

class MemberStatsStateTest {

    private fun change(id: String, value: Int) = MatchRatingChange(matchId = id, scheduledAt = Instant.parse("2026-10-01T18:00:00Z"), change = value)

    @Test
    fun `AC-018-06 the evolution rebuilds the rating after each match, oldest first`() {
        val state = MemberStatsState(rating = 1061, recentChanges = listOf(change("m3", 12), change("m2", -3)))
        assertEquals(listOf(1052, 1049, 1061), state.evolution)
    }

    @Test
    fun `AC-018-06 without rating there is no evolution`() {
        assertTrue(MemberStatsState(recentChanges = listOf(change("m1", 5))).evolution.isEmpty())
    }
}
