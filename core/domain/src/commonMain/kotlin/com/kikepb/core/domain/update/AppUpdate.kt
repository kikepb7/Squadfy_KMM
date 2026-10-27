package com.kikepb.core.domain.update

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/** The version installed on this device (`VERSION_NAME`, e.g. "1.4.0"). */
data class AppInfo(val versionName: String)

/**
 * Spec 021 APP-RN-P8: the versions published in Remote Config for this platform and environment. Blank when the
 * parameter is not published, so an unreachable Remote Config never blocks the app.
 */
data class AppVersionConfig(
    val latestVersion: String = "",
    val minVersion: String = "",
    val storeUrl: String = ""
)

interface AppVersionSource {
    fun observeVersions(): Flow<AppVersionConfig>
}

/** Builds without Remote Config (the frozen own-backend variant): never asks to update. */
object NoAppVersionSource : AppVersionSource {
    override fun observeVersions(): Flow<AppVersionConfig> = flowOf(AppVersionConfig())
}

/** Remembers the optional update the user dismissed, so it is not offered again until a newer one (APP-RN-P8). */
interface UpdatePromptStore {
    fun observeDismissedVersion(): Flow<String?>
    suspend fun dismiss(version: String)
}

enum class UpdateStatus { UP_TO_DATE, OPTIONAL, REQUIRED }

object AppUpdatePolicy {

    /**
     * REQUIRED below `min_version`; OPTIONAL below `latest_version` unless that version was dismissed; UP_TO_DATE
     * otherwise, and whenever a version cannot be read.
     */
    fun status(installed: String, config: AppVersionConfig, dismissedVersion: String? = null): UpdateStatus {
        val current = parse(installed) ?: return UpdateStatus.UP_TO_DATE
        val min = parse(config.minVersion)
        val latest = parse(config.latestVersion)
        return when {
            min != null && compare(current, min) < 0 -> UpdateStatus.REQUIRED
            latest != null && compare(current, latest) < 0 && config.latestVersion.trim() != dismissedVersion -> UpdateStatus.OPTIONAL
            else -> UpdateStatus.UP_TO_DATE
        }
    }

    /** "1.4.0", "1.4" or "1.4.0-beta" (the suffix is ignored) as numbers; null when it is not a version. */
    fun parse(version: String): List<Int>? {
        val core = version.trim().substringBefore('-').substringBefore('+')
        if (core.isEmpty()) return null
        return core.split('.').map { it.toIntOrNull() ?: return null }
    }

    private fun compare(a: List<Int>, b: List<Int>): Int {
        for (i in 0 until maxOf(a.size, b.size)) {
            val diff = a.getOrElse(i) { 0 } - b.getOrElse(i) { 0 }
            if (diff != 0) return diff
        }
        return 0
    }
}
