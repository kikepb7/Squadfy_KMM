package org.kikepb.squadfy.debug

import app.cash.turbine.test
import com.kikepb.core.domain.featureflag.AppEnvironment
import com.kikepb.core.domain.featureflag.FeatureFlag
import com.kikepb.core.domain.featureflag.FeatureFlagOverrides
import com.kikepb.core.domain.featureflag.FeatureFlagResolver
import com.kikepb.core.domain.featureflag.FeatureFlags
import com.kikepb.core.domain.featureflag.FlagValueSource
import com.kikepb.core.domain.featureflag.ResolvedFeatureFlag
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class FeatureFlagsViewModelTest {

    private class FakeFeatureFlags(override val environment: AppEnvironment) : FeatureFlags, FeatureFlagOverrides {
        val overrides = MutableStateFlow<Map<String, Boolean>>(emptyMap())
        private val resolved = overrides.map { FeatureFlagResolver.resolveAll(environment, overrides = it) }

        override fun isEnabled(flag: FeatureFlag) =
            FeatureFlagResolver.resolve(flag, environment, emptyMap(), overrides.value).enabled
        override fun observe(flag: FeatureFlag): Flow<Boolean> = resolved.map { list -> list.first { it.flag == flag }.enabled }
        override fun observeAll(): Flow<List<ResolvedFeatureFlag>> = resolved
        override suspend fun setOverride(flag: FeatureFlag, enabled: Boolean?) {
            overrides.value = if (enabled == null) overrides.value - flag.key else overrides.value + (flag.key to enabled)
        }
        override suspend fun clearOverrides() { overrides.value = emptyMap() }
    }

    private val flags = FakeFeatureFlags(AppEnvironment.PRE)

    @BeforeTest
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `AC-013-06 state lists every flag with its value and source`() = runTest {
        val viewModel = FeatureFlagsViewModel(featureFlags = flags, overrides = flags)

        viewModel.state.test {
            val state = awaitItem().takeIf { it.flags.isNotEmpty() } ?: awaitItem()
            assertEquals(AppEnvironment.PRE, state.environment)
            assertEquals(FeatureFlag.entries.size, state.flags.size)
            assertTrue(state.flags.all { it.source == FlagValueSource.DEFAULT })
        }
    }

    @Test
    fun `AC-013-06 toggling stores an override and toggling back to the default clears it`() = runTest {
        val viewModel = FeatureFlagsViewModel(featureFlags = flags, overrides = flags)

        viewModel.onAction(FeatureFlagsAction.OnToggle(FeatureFlag.MANUAL_SCORE, enabled = false))
        assertEquals(mapOf(FeatureFlag.MANUAL_SCORE.key to false), flags.overrides.value)

        viewModel.onAction(FeatureFlagsAction.OnToggle(FeatureFlag.MANUAL_SCORE, enabled = true))
        assertTrue(flags.overrides.value.isEmpty())
    }

    @Test
    fun `AC-013-05 reset clears every override`() = runTest {
        val viewModel = FeatureFlagsViewModel(featureFlags = flags, overrides = flags)
        viewModel.onAction(FeatureFlagsAction.OnToggle(FeatureFlag.HOME_NEWS, enabled = false))
        viewModel.onAction(FeatureFlagsAction.OnToggle(FeatureFlag.MANUAL_SCORE, enabled = false))

        viewModel.onAction(FeatureFlagsAction.OnReset)

        assertTrue(flags.overrides.value.isEmpty())
    }
}
