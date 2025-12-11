plugins {
    alias(libs.plugins.convention.cmp.library)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.kotlin.stdlib)

                implementation(projects.core.domain)

                implementation(libs.material3.adaptive)

                implementation(compose.components.resources)

                implementation(libs.jetbrains.navigation3.ui)
                implementation(libs.jetbrains.lifecycle.viewmodel.nav3)
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