package com.kikepb.squadfy.convention

import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/**
 * Shared test stack for every KMP module, so a new `commonTest` / `androidUnitTest`
 * source set compiles without per-module boilerplate.
 */
internal fun Project.configureKmpTestDependencies() {
    dependencies {
        "commonTestImplementation"(libs.findLibrary("kotlin-test").get())
        "commonTestImplementation"(libs.findLibrary("kotlinx-coroutines-test").get())
        "commonTestImplementation"(libs.findLibrary("turbine").get())
        "commonTestImplementation"(libs.findLibrary("ktor-client-mock").get())
        "androidUnitTestImplementation"(libs.findLibrary("junit4").get())
    }
}
