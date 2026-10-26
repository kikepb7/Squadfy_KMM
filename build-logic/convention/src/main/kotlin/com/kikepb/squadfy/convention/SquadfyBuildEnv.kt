package com.kikepb.squadfy.convention

import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties
import org.gradle.api.Project

/** Build environment for feature flag defaults (spec 013) and the Firebase app it talks to (spec 019 D-14). */
enum class SquadfyEnvironment(val key: String, val flavor: String) {
    PRE("pre", "dev"),
    PRO("pro", "prod");

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

private val devTask = Regex("Dev(Debug|Release)")
private val prodTask = Regex("Prod(Debug|Release)")

/**
 * The environment of this Gradle invocation. The Android app has `dev` and `prod` flavors, so a flavored task decides
 * (`assembleDevDebug` → PRE, `bundleProdRelease` → PRO): what you pick in Android Studio's Build Variants is what you
 * build. Without a flavored task (shared modules, Xcode, unit tests), an explicit `SQUADFY_ENV` wins, then debug → PRE
 * and release → PRO, so a store build never ships the PRE-only feature flags screen by accident (spec 018 T-022).
 *
 * BuildKonfig constants are shared by every module, so one invocation cannot build both flavors.
 */
val Project.squadfyEnvironment: SquadfyEnvironment
    get() {
        val tasks = gradle.startParameter.taskNames
        val dev = tasks.any { devTask.containsMatchIn(it) }
        val prod = tasks.any { prodTask.containsMatchIn(it) }
        check(!(dev && prod)) { "Build the dev and prod flavors in separate Gradle invocations (they share BuildKonfig)" }
        return when {
            dev -> SquadfyEnvironment.PRE
            prod -> SquadfyEnvironment.PRO
            else -> SquadfyEnvironment.fromKey(squadfyProperty("SQUADFY_ENV") ?: if (isSquadfyReleaseBuild) "pro" else "pre")
        }
    }

val Project.squadfyBackend: SquadfyBackend
    get() = SquadfyBackend.fromKey(squadfyProperty("SQUADFY_BACKEND") ?: SquadfyBackend.FIREBASE.key)
