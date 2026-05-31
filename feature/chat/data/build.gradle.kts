import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    alias(libs.plugins.convention.kmp.library)
    alias(libs.plugins.convention.buildkonfig)
}


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
            dependencies {
                implementation(libs.koin.android)
                implementation(libs.androidx.lifecycle.process)

                implementation(project.dependencies.platform(libs.firebase.bom))
                implementation(libs.firebase.messaging)
                implementation(libs.koin.android)
            }
        }

        iosMain {
            dependencies {}
        }

        commonTest {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.kotlinx.coroutines.test)
            }
        }

        androidUnitTest {
            dependencies {
                implementation(libs.junit4)
                implementation(libs.kotlinx.coroutines.test)
                implementation(libs.ktor.client.mock)
            }
        }
    }

    targets.withType<KotlinNativeTarget> {
        compilations.getByName("main") {
            cinterops {
                create("network") {
                    defFile(file("src/nativeInterop/cinterop/network.def"))
                }
            }
        }
    }
}