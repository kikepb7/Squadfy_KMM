package com.kikepb.core.domain.featureflag

import kotlinx.coroutines.flow.Flow

/** Read access to feature flags. Inject it in ViewModels and expose the values through their State. */
interface FeatureFlags {
    val environment: AppEnvironment
    val platform: AppPlatform get() = AppPlatform.ANDROID
    fun isEnabled(flag: FeatureFlag): Boolean
    fun observe(flag: FeatureFlag): Flow<Boolean>
    fun observeAll(): Flow<List<ResolvedFeatureFlag>>
}

/** Local overrides for QA. Only effective in [AppEnvironment.PRE]; no-ops in PRO. */
interface FeatureFlagOverrides {
    suspend fun setOverride(flag: FeatureFlag, enabled: Boolean?)
    suspend fun clearOverrides()
}

/** Remote values (Firebase Remote Config or a backend endpoint, decision D-10), keyed by [FeatureFlag.key]. */
interface RemoteFeatureFlagSource {
    fun observe(): Flow<Map<String, Boolean>>
}
