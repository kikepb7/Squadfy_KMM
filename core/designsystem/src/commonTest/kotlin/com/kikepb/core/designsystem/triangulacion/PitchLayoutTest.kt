package com.kikepb.core.designsystem.triangulacion

import com.kikepb.core.designsystem.triangulacion.components.PitchLayout
import com.kikepb.core.designsystem.triangulacion.components.PitchPlayer
import com.kikepb.core.designsystem.triangulacion.components.TriPosition
import com.kikepb.core.designsystem.triangulacion.components.TriTeam
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PitchLayoutTest {

    private fun team(prefix: String) = listOf(
        PitchPlayer("${prefix}1", "GK", 1, TriPosition.GK),
        PitchPlayer("${prefix}4", "D1", 4, TriPosition.DEF), PitchPlayer("${prefix}5", "D2", 5, TriPosition.DEF), PitchPlayer("${prefix}3", "D3", 3, TriPosition.DEF),
        PitchPlayer("${prefix}8", "M1", 8, TriPosition.MID), PitchPlayer("${prefix}10", "M2", 10, TriPosition.MID),
        PitchPlayer("${prefix}9", "F1", 9, TriPosition.FWD)
    )

    private fun near(a: Float, b: Float) = abs(a - b) < 0.001f

    @Test
    fun `AC-018-07 a 1-3-2-1 line-up stands like the design, team A on top and team B mirrored`() {
        val slots = PitchLayout.place(team("a"), team("b"))
        val defendersA = slots.filter { it.team == TriTeam.A && it.player.position == TriPosition.DEF }.map { it.x }
        val midsA = slots.filter { it.team == TriTeam.A && it.player.position == TriPosition.MID }.map { it.x }
        assertTrue(defendersA.zip(listOf(0.2f, 0.5f, 0.8f)).all { (x, e) -> near(x, e) }, "defenders $defendersA")
        assertTrue(midsA.zip(listOf(0.33f, 0.67f)).all { (x, e) -> near(x, e) }, "midfielders $midsA")
        val gkA = slots.first { it.team == TriTeam.A && it.isGoalkeeper }
        val gkB = slots.first { it.team == TriTeam.B && it.isGoalkeeper }
        assertTrue(gkA.y < 0.12f && gkB.y > 0.88f)
    }

    @Test
    fun `AC-018-07 passes join close players of consecutive lines, never the goalkeeper or the same line`() {
        val slots = PitchLayout.place(team("a"), emptyList())
        val links = PitchLayout.links(slots)
        assertEquals(6, links.size)
        assertTrue(links.none { (a, b) -> a.isGoalkeeper || b.isGoalkeeper })
        assertTrue(links.none { (a, b) -> a.line == b.line })
    }

    @Test
    fun `AC-018-07 players without position play in midfield and big lines split in two`() {
        val many = (1..8).map { PitchPlayer("p$it", "P$it", it, null) }
        val slots = PitchLayout.place(many, emptyList())
        assertEquals(2, slots.map { it.y }.distinct().size)
    }
}
