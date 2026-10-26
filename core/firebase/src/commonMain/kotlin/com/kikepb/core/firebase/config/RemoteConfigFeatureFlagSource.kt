package com.kikepb.core.firebase.config

import com.kikepb.core.domain.featureflag.FeatureFlag
import com.kikepb.core.domain.featureflag.RemoteFeatureFlagSource
import com.kikepb.core.domain.logger.SquadfyLogger
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
 */
class RemoteConfigFeatureFlagSource(
    private val remoteConfig: Lazy<FirebaseRemoteConfig>,
    private val minimumFetchInterval: Duration,
    private val logger: SquadfyLogger
) : RemoteFeatureFlagSource {

    private val values = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    private val mutex = Mutex()
    private var isSetUp = false

    override fun observe(): Flow<Map<String, Boolean>> = values.asStateFlow()

    /** Publishes what a previous launch activated, then fetches and activates the latest values. */
    suspend fun refresh() = mutex.withLock {
        val config = remoteConfig.value
        if (!isSetUp) {
            safeFirebaseCall {
                config.settings { minimumFetchInterval = this@RemoteConfigFeatureFlagSource.minimumFetchInterval }
                config.ensureInitialized()
            }
            isSetUp = true
            values.value = config.readFlags()
        }
        safeFirebaseCall { config.fetchAndActivate() }
            .let { result -> if (result is com.kikepb.core.domain.util.Result.Failure) logger.warn("Remote Config fetch failed: ${result.error}") }
        values.value = config.readFlags()
    }

    private fun FirebaseRemoteConfig.readFlags(): Map<String, Boolean> =
        FeatureFlag.entries.mapNotNull { flag ->
            val value = getValue(flag.key)
            if (value.getSource() == ValueSource.Remote) flag.key to value.asBoolean() else null
        }.toMap()
}
