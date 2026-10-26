plugins {
    alias(libs.plugins.convention.kmp.library)
}

// Spec 019: DTOs and mappers of the backend contract (BACKEND.md §9), shared by the own-backend data layer and the
// Firebase one, whose Cloud Functions answer the same JSON
kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.core.domain)
                implementation(projects.feature.club.domain)
                implementation(libs.kotlinx.datetime)
            }
        }
    }
}
