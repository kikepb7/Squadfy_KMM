package com.kikepb.core.domain.featureflag

enum class FlagValueSource { DEFAULT, REMOTE, OVERRIDE }

data class ResolvedFeatureFlag(
    val flag: FeatureFlag,
    val enabled: Boolean,
    val source: FlagValueSource
)

/**
 * APP-RN-16: local override (PRE only) → remote value → environment default.
 * Maps are keyed by [FeatureFlag.key] so unknown remote keys are simply ignored.
 */
object FeatureFlagResolver {

    fun resolve(
        flag: FeatureFlag,
        environment: AppEnvironment,
        remote: Map<String, Boolean>,
        overrides: Map<String, Boolean>,
        platform: AppPlatform = AppPlatform.ANDROID
    ): ResolvedFeatureFlag {
        val override = overrides[flag.key].takeIf { environment == AppEnvironment.PRE }
        val remoteValue = remote[flag.key]
        return when {
            override != null -> ResolvedFeatureFlag(flag = flag, enabled = override, source = FlagValueSource.OVERRIDE)
            remoteValue != null -> ResolvedFeatureFlag(flag = flag, enabled = remoteValue, source = FlagValueSource.REMOTE)
            else -> ResolvedFeatureFlag(flag = flag, enabled = flag.defaultFor(environment, platform), source = FlagValueSource.DEFAULT)
        }
    }

    fun resolveAll(
        environment: AppEnvironment,
        remote: Map<String, Boolean> = emptyMap(),
        overrides: Map<String, Boolean> = emptyMap(),
        platform: AppPlatform = AppPlatform.ANDROID
    ): List<ResolvedFeatureFlag> = FeatureFlag.entries.map { resolve(it, environment, remote, overrides, platform) }
}
