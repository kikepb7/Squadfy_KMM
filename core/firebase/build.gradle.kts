plugins {
    alias(libs.plugins.convention.kmp.library)
    alias(libs.plugins.convention.buildkonfig)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.core.domain)

                implementation(libs.koin.core)
                implementation(libs.touchlab.kermit)

                // Spec 019 (as in TensoScan): the feature `data-firebase` modules declare the Firebase products they
                // use; Analytics, Crashlytics and Remote Config are only reachable through core/domain contracts
                api(libs.gitlive.firebase.app)
                api(libs.gitlive.firebase.auth)
                api(libs.gitlive.firebase.firestore)
                api(libs.gitlive.firebase.functions)
                implementation(libs.gitlive.firebase.database)
                implementation(libs.gitlive.firebase.storage)
                implementation(libs.gitlive.firebase.analytics)
                implementation(libs.gitlive.firebase.crashlytics)
                implementation(libs.gitlive.firebase.config)
            }
        }

        androidMain {
            dependencies {
                implementation(libs.koin.android)
            }
        }

        iosMain {
            dependencies {
                // Real Kotlin stack traces for iOS crashes in Crashlytics
                implementation(libs.touchlab.crashkios.crashlytics)
            }
        }
    }
}
