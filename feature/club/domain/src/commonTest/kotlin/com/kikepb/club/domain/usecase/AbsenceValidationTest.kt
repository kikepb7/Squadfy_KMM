package com.kikepb.club.domain.usecase

import com.kikepb.club.domain.model.MemberAbsenceModel
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AbsenceValidationTest {

    private val today = LocalDate(2026, 10, 7)

    @Test
    fun `AC-014-02 a valid period passes, including one already started`() {
        assertNull(AddMyAbsenceUseCase.validate(LocalDate(2026, 10, 1), LocalDate(2026, 10, 20), "Vacaciones", today))
        assertNull(AddMyAbsenceUseCase.validate(today, today, null, today))
    }

    @Test
    fun `AC-014-02 inverted, past, longer than a year or long reasons are rejected`() {
        assertEquals(AbsenceValidationError.END_BEFORE_START, AddMyAbsenceUseCase.validate(LocalDate(2026, 10, 10), LocalDate(2026, 10, 9), null, today))
        assertEquals(AbsenceValidationError.ENDS_IN_PAST, AddMyAbsenceUseCase.validate(LocalDate(2026, 10, 1), LocalDate(2026, 10, 6), null, today))
        assertEquals(AbsenceValidationError.TOO_LONG, AddMyAbsenceUseCase.validate(LocalDate(2026, 10, 8), LocalDate(2027, 10, 9), null, today))
        assertEquals(AbsenceValidationError.REASON_TOO_LONG, AddMyAbsenceUseCase.validate(today, today, "x".repeat(201), today))
    }

    @Test
    fun `AC-014-05 only my own absence covering the match day counts`() {
        val absences = listOf(
            MemberAbsenceModel("a", "me", LocalDate(2026, 10, 10), LocalDate(2026, 10, 15), null),
            MemberAbsenceModel("b", "other", LocalDate(2026, 10, 20), LocalDate(2026, 10, 20), null)
        )
        assertTrue(absences.coversMe("me", LocalDate(2026, 10, 15)))
        assertFalse(absences.coversMe("me", LocalDate(2026, 10, 20)))
        assertFalse(absences.coversMe(null, LocalDate(2026, 10, 12)))
    }
}
