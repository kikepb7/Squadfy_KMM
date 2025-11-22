plugins {
    alias(libs.plugins.convention.kmp.library)
    alias(libs.plugins.convention.buildkonfig)}


kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.kotlin.stdlib)

                implementation(projects.core.data)
                implementation(projects.core.domain)
                implementation(projects.feature.chat.domain)
                implementation(projects.feature.chat.database)

                implementation(libs.bundles.ktor.common)
                implementation(libs.koin.core)
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