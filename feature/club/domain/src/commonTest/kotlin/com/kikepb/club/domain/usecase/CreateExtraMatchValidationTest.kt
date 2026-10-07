package com.kikepb.club.domain.usecase

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Instant

class CreateExtraMatchValidationTest {

    private val now = Instant.parse("2026-10-07T12:00:00Z")
    private val later = Instant.parse("2026-10-07T12:15:00Z")

    @Test
    fun `AC-007-08 a future match with no or valid duration is accepted`() {
        assertNull(CreateExtraMatchUseCase.validate(later, durationMinutes = null, now = now))
        assertNull(CreateExtraMatchUseCase.validate(later, durationMinutes = 90, now = now))
    }

    @Test
    fun `AC-007-08 past dates and durations outside 10-180 are rejected`() {
        assertEquals(ExtraMatchError.NOT_IN_FUTURE, CreateExtraMatchUseCase.validate(now, durationMinutes = null, now = now))
        assertEquals(ExtraMatchError.INVALID_DURATION, CreateExtraMatchUseCase.validate(later, durationMinutes = 5, now = now))
        assertEquals(ExtraMatchError.INVALID_DURATION, CreateExtraMatchUseCase.validate(later, durationMinutes = 181, now = now))
    }
}
