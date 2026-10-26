package com.kikepb.squadfy.convention

import org.gradle.api.Project
import java.util.Locale

fun Project.pathToPackageName(): String {
    // Module names may contain hyphens (`data-firebase`), which are not valid in a package name
    val relativePackageName = path
        .replace(':', '.')
        .replace("-", "")
        .lowercase()

    return "com.kikepb$relativePackageName"
}

fun Project.pathToResourcePrefix(): String {
    return path
        .replace(':', '_')
        .replace('-', '_')
        .lowercase()
        .drop(1) + "_"
}

fun Project.pathToFrameworkName(): String {
    val parts = this.path.split(":", "-", "_", " ")
    return parts.joinToString("") { part ->
        part.replaceFirstChar {
            it.titlecase(Locale.ROOT)
        }
    }
}