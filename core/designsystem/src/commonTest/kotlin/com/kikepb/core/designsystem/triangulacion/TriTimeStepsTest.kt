package com.kikepb.core.designsystem.triangulacion

import com.kikepb.core.designsystem.triangulacion.components.TriTimeSteps
import kotlinx.datetime.LocalTime
import kotlin.test.Test
import kotlin.test.assertEquals

class TriTimeStepsTest {

    @Test
    fun `AC-020-10 plus and minus move by quarters of an hour`() {
        assertEquals(LocalTime(20, 15), TriTimeSteps.next(LocalTime(20, 0)))
        assertEquals(LocalTime(19, 45), TriTimeSteps.previous(LocalTime(20, 0)))
    }

    @Test
    fun `AC-020-10 a time off the quarter snaps to the next or previous quarter`() {
        assertEquals(LocalTime(20, 15), TriTimeSteps.next(LocalTime(20, 5)))
        assertEquals(LocalTime(20, 0), TriTimeSteps.previous(LocalTime(20, 5)))
    }

    @Test
    fun `AC-020-10 the steps wrap around midnight`() {
        assertEquals(LocalTime(0, 0), TriTimeSteps.next(LocalTime(23, 45)))
        assertEquals(LocalTime(23, 45), TriTimeSteps.previous(LocalTime(0, 0)))
    }

    @Test
    fun `AC-020-10 the grid keeps the other part and the time shows with two digits`() {
        assertEquals(LocalTime(9, 30), TriTimeSteps.withHour(LocalTime(20, 30), 9))
        assertEquals(LocalTime(20, 5), TriTimeSteps.withMinute(LocalTime(20, 30), 5))
        assertEquals("09:05", TriTimeSteps.format(LocalTime(9, 5)))
        assertEquals(12, TriTimeSteps.MINUTES.size)
    }
}
