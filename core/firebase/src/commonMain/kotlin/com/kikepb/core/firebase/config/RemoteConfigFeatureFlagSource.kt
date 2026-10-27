package com.kikepb.core.firebase.config

import com.kikepb.core.domain.featureflag.FeatureFlag
import com.kikepb.core.domain.featureflag.RemoteFeatureFlagSource
import com.kikepb.core.domain.logger.SquadfyLogger
import com.kikepb.core.domain.update.AppVersionConfig
import com.kikepb.core.domain.update.AppVersionSource
import com.kikepb.core.firebase.error.safeFirebaseCall
import dev.gitlive.firebase.remoteconfig.FirebaseRemoteConfig
import dev.gitlive.firebase.remoteconfig.ValueSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Duration

/**
 * Remote feature flags from Firebase Remote Config (spec 019 AC-019-19, closes D-10). Each [FeatureFlag.key] is a
 * boolean parameter in `firebase/remoteconfig.template.json`. Only values actually set in the console count as remote:
 * a parameter that is not published keeps the environment default of spec 013.
 *
 * Spec 021 APP-RN-P8: it also publishes the version parameters (`latest_version`, `min_version`, `store_url`), whose
 * values differ per platform and environment through the template conditions (`entorno_pre`, `entorno_pro`, `android`, `ios`).
 */
class RemoteConfigFeatureFlagSource(
    private val remoteConfig: Lazy<FirebaseRemoteConfig>,
    private val minimumFetchInterval: Duration,
    private val logger: SquadfyLogger
) : RemoteFeatureFlagSource, AppVersionSource {

    private val values = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    private val versions = MutableStateFlow(AppVersionConfig())
    private val mutex = Mutex()
    private var isSetUp = false

    override fun observe(): Flow<Map<String, Boolean>> = values.asStateFlow()

    override fun observeVersions(): Flow<AppVersionConfig> = versions.asStateFlow()

    /** Publishes what a previous launch activated, then fetches and activates the latest values. */
    suspend fun refresh() = mutex.withLock {
        val config = remoteConfig.value
        if (!isSetUp) {
            safeFirebaseCall {
                config.settings { minimumFetchInterval = this@RemoteConfigFeatureFlagSource.minimumFetchInterval }
                config.ensureInitialized()
            }
            isSetUp = true
            publish(config)
        }
        safeFirebaseCall { config.fetchAndActivate() }
            .let { result -> if (result is com.kikepb.core.domain.util.Result.Failure) logger.warn("Remote Config fetch failed: ${result.error}") }
        publish(config)
    }

    private fun publish(config: FirebaseRemoteConfig) {
        values.value = config.readFlags()
        versions.value = AppVersionConfig(
            latestVersion = config.readRemoteString(KEY_LATEST_VERSION),
            minVersion = config.readRemoteString(KEY_MIN_VERSION),
            storeUrl = config.readRemoteString(KEY_STORE_URL)
        )
    }

    private fun FirebaseRemoteConfig.readRemoteString(key: String): String =
        getValue(key).takeIf { it.getSource() == ValueSource.Remote }?.asString()?.trim().orEmpty()

    private fun FirebaseRemoteConfig.readFlags(): Map<String, Boolean> =
        FeatureFlag.entries.mapNotNull { flag ->
            val value = getValue(flag.key)
            if (value.getSource() == ValueSource.Remote) flag.key to value.asBoolean() else null
        }.toMap()

    private companion object {
        const val KEY_LATEST_VERSION = "latest_version"
        const val KEY_MIN_VERSION = "min_version"
        const val KEY_STORE_URL = "store_url"
    }
}
