package com.kikepb.club.domain.usecase

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GenerateTeamsValidationTest {

    @Test
    fun `AC-006-05 a balanced disjoint split is valid`() {
        assertTrue(GenerateTeamsUseCase.isValidManualSplit(listOf("a", "b", "g-1"), listOf("c", "d")))
    }

    @Test
    fun `AC-006-05 empty team, overlap or size gap over one are invalid`() {
        assertFalse(GenerateTeamsUseCase.isValidManualSplit(emptyList(), listOf("a")))
        assertFalse(GenerateTeamsUseCase.isValidManualSplit(listOf("a", "b"), listOf("b", "c")))
        assertFalse(GenerateTeamsUseCase.isValidManualSplit(listOf("a", "b", "c"), listOf("d")))
    }
}
