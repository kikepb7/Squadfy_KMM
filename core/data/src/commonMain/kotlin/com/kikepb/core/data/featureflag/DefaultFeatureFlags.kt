package com.kikepb.core.data.featureflag

import com.kikepb.core.domain.featureflag.AppEnvironment
import com.kikepb.core.domain.featureflag.FeatureFlag
import com.kikepb.core.domain.featureflag.FeatureFlagOverrides
import com.kikepb.core.domain.featureflag.FeatureFlagResolver
import com.kikepb.core.domain.featureflag.FeatureFlags
import com.kikepb.core.domain.featureflag.RemoteFeatureFlagSource
import com.kikepb.core.domain.featureflag.ResolvedFeatureFlag
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class DefaultFeatureFlags(
    override val environment: AppEnvironment,
    private val overrideStore: FeatureFlagOverrideStore,
    remoteSource: RemoteFeatureFlagSource,
    scope: CoroutineScope
) : FeatureFlags, FeatureFlagOverrides {

    private val resolved: StateFlow<List<ResolvedFeatureFlag>> = combine(
        overrideStore.observe(),
        remoteSource.observe()
    ) { overrides, remote ->
        FeatureFlagResolver.resolveAll(environment = environment, remote = remote, overrides = overrides)
    }.stateIn(
        scope = scope,
        started = SharingStarted.Eagerly,
        initialValue = FeatureFlagResolver.resolveAll(environment = environment)
    )

    override fun isEnabled(flag: FeatureFlag): Boolean =
        resolved.value.first { it.flag == flag }.enabled

    override fun observe(flag: FeatureFlag): Flow<Boolean> =
        resolved.map { flags -> flags.first { it.flag == flag }.enabled }.distinctUntilChanged()

    override fun observeAll(): Flow<List<ResolvedFeatureFlag>> = resolved

    override suspend fun setOverride(flag: FeatureFlag, enabled: Boolean?) {
        if (environment != AppEnvironment.PRE) return
        overrideStore.set(key = flag.key, enabled = enabled)
    }

    override suspend fun clearOverrides() {
        if (environment != AppEnvironment.PRE) return
        overrideStore.clear()
    }
}

/** Placeholder until the remote layer is chosen (decision D-10). */
class NoOpRemoteFeatureFlagSource : RemoteFeatureFlagSource {
    override fun observe(): Flow<Map<String, Boolean>> = flowOf(emptyMap())
}
