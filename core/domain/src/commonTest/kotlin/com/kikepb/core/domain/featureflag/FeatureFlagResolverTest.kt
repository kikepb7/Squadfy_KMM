package com.kikepb.core.domain.featureflag

import com.kikepb.core.domain.featureflag.AppEnvironment.PRE
import com.kikepb.core.domain.featureflag.AppEnvironment.PRO
import com.kikepb.core.domain.featureflag.FeatureFlag.DEV_TEST_MATCH
import com.kikepb.core.domain.featureflag.FeatureFlag.MATCH_GUESTS
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class FeatureFlagResolverTest {

    private data class Case(
        val environment: AppEnvironment,
        val remote: Boolean?,
        val override: Boolean?,
        val expected: Boolean,
        val expectedSource: FlagValueSource
    )

    @Test
    fun `AC-013-03 resolution order is override in PRE then remote then environment default`() {
        // DEV_TEST_MATCH defaults: PRE = true, PRO = false
        val cases = listOf(
            Case(PRE, remote = null, override = null, expected = true, expectedSource = FlagValueSource.DEFAULT),
            Case(PRO, remote = null, override = null, expected = false, expectedSource = FlagValueSource.DEFAULT),
            Case(PRE, remote = false, override = null, expected = false, expectedSource = FlagValueSource.REMOTE),
            Case(PRO, remote = true, override = null, expected = true, expectedSource = FlagValueSource.REMOTE),
            Case(PRE, remote = false, override = true, expected = true, expectedSource = FlagValueSource.OVERRIDE),
            Case(PRE, remote = null, override = false, expected = false, expectedSource = FlagValueSource.OVERRIDE),
            // Overrides are ignored in PRO
            Case(PRO, remote = null, override = true, expected = false, expectedSource = FlagValueSource.DEFAULT),
            Case(PRO, remote = true, override = false, expected = true, expectedSource = FlagValueSource.REMOTE)
        )

        cases.forEach { case ->
            val resolved = FeatureFlagResolver.resolve(
                flag = DEV_TEST_MATCH,
                environment = case.environment,
                remote = case.remote?.let { mapOf(DEV_TEST_MATCH.key to it) }.orEmpty(),
                overrides = case.override?.let { mapOf(DEV_TEST_MATCH.key to it) }.orEmpty()
            )
            assertEquals(case.expected, resolved.enabled, "enabled for $case")
            assertEquals(case.expectedSource, resolved.source, "source for $case")
        }
    }

    @Test
    fun `AC-013-03 values for other flags do not leak`() {
        val resolved = FeatureFlagResolver.resolve(
            flag = MATCH_GUESTS,
            environment = PRE,
            remote = mapOf(DEV_TEST_MATCH.key to true, "unknown_flag" to true),
            overrides = mapOf(DEV_TEST_MATCH.key to true)
        )
        assertEquals(false, resolved.enabled)
        assertEquals(FlagValueSource.DEFAULT, resolved.source)
    }

    @Test
    fun `AC-013-07 backend pending flags are off in both environments`() {
        listOf(FeatureFlag.MATCH_GUESTS, FeatureFlag.SCHEDULE_EXCEPTIONS, FeatureFlag.CUSTOM_DRAW_TIME, FeatureFlag.MANUAL_SCORE)
            .forEach { flag ->
                assertEquals(false, flag.defaultFor(PRE), "${flag.name} in PRE")
                assertEquals(false, flag.defaultFor(PRO), "${flag.name} in PRO")
            }
    }

    @Test
    fun `AC-013-02 flag keys are unique`() {
        val keys = FeatureFlag.entries.map { it.key }
        assertEquals(keys.size, keys.toSet().size, "duplicated keys: ${keys.groupBy { it }.filterValues { it.size > 1 }.keys}")
    }

    @Test
    fun `AC-013-01 environment parsing accepts pre and pro only`() {
        assertEquals(PRE, AppEnvironment.fromKey("pre"))
        assertEquals(PRO, AppEnvironment.fromKey("PRO"))
        assertFailsWith<IllegalArgumentException> { AppEnvironment.fromKey("staging") }
    }
}
