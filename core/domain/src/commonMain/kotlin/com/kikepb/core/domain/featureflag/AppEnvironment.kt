package com.kikepb.core.domain.featureflag

/**
 * Build environment the app was compiled for (`SQUADFY_ENV`).
 * PRE is used for QA and allows local feature flag overrides; PRO is what users get.
 */
enum class AppEnvironment(val key: String) {
    PRE(key = "pre"),
    PRO(key = "pro");

    companion object {
        fun fromKey(key: String): AppEnvironment =
            entries.firstOrNull { it.key.equals(key, ignoreCase = true) }
                ?: throw IllegalArgumentException("Unknown environment '$key'. Expected one of ${entries.map { it.key }}")
    }
}
