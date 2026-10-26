plugins {
    alias(libs.plugins.convention.kmp.library)
}

// Spec 019: chat, participants, presence and device tokens on Firebase (Firestore, Realtime Database, Storage)
kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.core.domain)
                implementation(projects.core.firebase)
                implementation(projects.feature.chat.domain)
                implementation(libs.gitlive.firebase.database)
                implementation(libs.kotlinx.datetime)
                implementation(libs.koin.core)
            }
        }
    }
}
