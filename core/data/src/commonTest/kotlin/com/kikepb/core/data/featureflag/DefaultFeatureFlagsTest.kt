package com.kikepb.core.data.featureflag

import app.cash.turbine.test
import com.kikepb.core.domain.featureflag.AppEnvironment
import com.kikepb.core.domain.featureflag.AppPlatform
import com.kikepb.core.domain.featureflag.FeatureFlag
import com.kikepb.core.domain.featureflag.FlagValueSource
import com.kikepb.core.domain.featureflag.RemoteFeatureFlagSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DefaultFeatureFlagsTest {

    private class FakeOverrideStore : FeatureFlagOverrideStore {
        val values = MutableStateFlow<Map<String, Boolean>>(emptyMap())
        override fun observe(): Flow<Map<String, Boolean>> = values
        override suspend fun set(key: String, enabled: Boolean?) {
            values.value = if (enabled == null) values.value - key else values.value + (key to enabled)
        }
        override suspend fun clear() { values.value = emptyMap() }
    }

    private class FakeRemoteSource : RemoteFeatureFlagSource {
        val values = MutableStateFlow<Map<String, Boolean>>(emptyMap())
        override fun observe(): Flow<Map<String, Boolean>> = values
    }

    private val overrideStore = FakeOverrideStore()
    private val remoteSource = FakeRemoteSource()

    private fun TestScope.createFlags(environment: AppEnvironment, platform: AppPlatform = AppPlatform.ANDROID) = DefaultFeatureFlags(
        environment = environment,
        platform = platform,
        overrideStore = overrideStore,
        remoteSource = remoteSource,
        scope = backgroundScope
    )

    @Test
    fun `AC-013-04 isEnabled returns the environment default when nothing else is set`() = runTest(UnconfinedTestDispatcher()) {
        assertTrue(createFlags(AppEnvironment.PRE).isEnabled(FeatureFlag.DEV_TEST_MATCH))
        assertFalse(createFlags(AppEnvironment.PRO).isEnabled(FeatureFlag.DEV_TEST_MATCH))
    }

    @Test
    fun `AC-013-04 observe emits when an override changes in PRE`() = runTest(UnconfinedTestDispatcher()) {
        val flags = createFlags(AppEnvironment.PRE)

        flags.observe(FeatureFlag.MANUAL_SCORE).test {
            assertTrue(awaitItem())
            flags.setOverride(FeatureFlag.MANUAL_SCORE, enabled = false)
            assertFalse(awaitItem())
            flags.setOverride(FeatureFlag.MANUAL_SCORE, enabled = null)
            assertTrue(awaitItem())
        }
        assertTrue(flags.isEnabled(FeatureFlag.MANUAL_SCORE))
    }

    @Test
    fun `AC-013-05 clearOverrides restores defaults`() = runTest(UnconfinedTestDispatcher()) {
        val flags = createFlags(AppEnvironment.PRE)
        flags.setOverride(FeatureFlag.HOME_NEWS, enabled = true)
        flags.setOverride(FeatureFlag.MANUAL_SCORE, enabled = false)

        flags.clearOverrides()

        assertFalse(flags.isEnabled(FeatureFlag.HOME_NEWS))
        assertTrue(flags.isEnabled(FeatureFlag.MANUAL_SCORE))
        assertTrue(overrideStore.values.value.isEmpty())
    }

    @Test
    fun `AC-013-03 PRO ignores and does not persist overrides`() = runTest(UnconfinedTestDispatcher()) {
        overrideStore.values.value = mapOf(FeatureFlag.MANUAL_SCORE.key to true)
        val flags = createFlags(AppEnvironment.PRO)

        flags.setOverride(FeatureFlag.HOME_NEWS, enabled = true)

        assertFalse(flags.isEnabled(FeatureFlag.MANUAL_SCORE))
        assertFalse(flags.isEnabled(FeatureFlag.HOME_NEWS))
        assertEquals(mapOf(FeatureFlag.MANUAL_SCORE.key to true), overrideStore.values.value)
    }

    @Test
    fun `AC-013-09 remote values apply in PRO`() = runTest(UnconfinedTestDispatcher()) {
        val flags = createFlags(AppEnvironment.PRO)

        remoteSource.values.value = mapOf(FeatureFlag.MANUAL_SCORE.key to true)

        assertTrue(flags.isEnabled(FeatureFlag.MANUAL_SCORE))
        flags.observeAll().test {
            assertEquals(FlagValueSource.REMOTE, awaitItem().first { it.flag == FeatureFlag.MANUAL_SCORE }.source)
        }
    }

    @Test
    fun `AC-015-02 chat is hidden on iOS in PRO and shown on Android and in PRE`() = runTest(UnconfinedTestDispatcher()) {
        assertTrue(createFlags(AppEnvironment.PRO, AppPlatform.ANDROID).isEnabled(FeatureFlag.CHAT))
        assertFalse(createFlags(AppEnvironment.PRO, AppPlatform.IOS).isEnabled(FeatureFlag.CHAT))
        assertTrue(createFlags(AppEnvironment.PRE, AppPlatform.IOS).isEnabled(FeatureFlag.CHAT))
    }
}
