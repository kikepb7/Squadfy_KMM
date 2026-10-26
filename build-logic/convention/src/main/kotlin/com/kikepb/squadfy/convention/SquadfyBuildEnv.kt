package com.kikepb.squadfy.convention

import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties
import org.gradle.api.Project

/** Build environment for feature flag defaults (spec 013) and the Firebase app it talks to (spec 019 D-14). */
enum class SquadfyEnvironment(val key: String, val applicationIdSuffix: String) {
    PRE("pre", ".dev"),
    PRO("pro", "");

    companion object {
        fun fromKey(key: String): SquadfyEnvironment = values().firstOrNull { it.key == key.lowercase() }
            ?: error("Invalid SQUADFY_ENV '$key'. Expected one of ${values().map { it.key }}")
    }
}

/** Data layer the app is built with (spec 019 AC-019-01, ADR-0010): Firebase (default) or the frozen own backend. */
enum class SquadfyBackend(val key: String) {
    FIREBASE("firebase"),
    API("api");

    companion object {
        fun fromKey(key: String): SquadfyBackend = values().firstOrNull { it.key == key.lowercase() }
            ?: error("Invalid SQUADFY_BACKEND '$key'. Expected one of ${values().map { it.key }}")
    }
}

/**
 * Lookup order: -Pkey / gradle.properties, then environment (CI secrets), then local.properties.
 * Blank values count as missing: GitHub expands an unset secret to "", which must not pass as a real value.
 */
fun Project.squadfyProperty(key: String): String? {
    val localProperties = gradleLocalProperties(rootDir, rootProject.providers)
    return providers.gradleProperty(key).orNull?.takeIf { it.isNotBlank() }
        ?: providers.environmentVariable(key).orNull?.takeIf { it.isNotBlank() }
        ?: localProperties.getProperty(key)?.takeIf { it.isNotBlank() }
}

/** Android `*Release*` tasks or an Xcode Release configuration. */
val Project.isSquadfyReleaseBuild: Boolean
    get() = gradle.startParameter.taskNames.any { it.contains("Release") } ||
        providers.environmentVariable("CONFIGURATION").orNull == "Release"

/**
 * Debug builds default to PRE and release builds to PRO, so a store build never ships the PRE-only feature flags
 * screen by accident (spec 018 T-022). An explicit `SQUADFY_ENV` still wins (e.g. a PRE release for testers).
 */
val Project.squadfyEnvironment: SquadfyEnvironment
    get() = SquadfyEnvironment.fromKey(squadfyProperty("SQUADFY_ENV") ?: if (isSquadfyReleaseBuild) "pro" else "pre")

val Project.squadfyBackend: SquadfyBackend
    get() = SquadfyBackend.fromKey(squadfyProperty("SQUADFY_BACKEND") ?: SquadfyBackend.FIREBASE.key)
