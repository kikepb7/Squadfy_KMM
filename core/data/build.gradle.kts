plugins {
    alias(libs.plugins.convention.kmp.library)
    alias(libs.plugins.convention.buildkonfig)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.kotlin.stdlib)

                implementation(projects.core.domain)
            }
        }

        androidMain {
            dependencies {}
        }

        iosMain {
            dependencies {}
        }
    }
}