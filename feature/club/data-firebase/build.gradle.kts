plugins {
    alias(libs.plugins.convention.kmp.library)
}

// Spec 019: the club data layer on Firebase (Firestore listeners for reads, Cloud Functions for writes)
kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.core.domain)
                implementation(projects.core.firebase)
                implementation(projects.feature.club.domain)
                implementation(projects.feature.club.dataContract)
                implementation(libs.kotlinx.datetime)
                implementation(libs.koin.core)
            }
        }
    }
}
