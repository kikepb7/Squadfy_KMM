plugins {
    alias(libs.plugins.convention.cmp.application)
    alias(libs.plugins.compose.hot.reload)
    alias(libs.plugins.google.services)
}

kotlin {
    sourceSets {
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)

            implementation(libs.core.splashscreen)

            implementation(libs.koin.android)
        }
        commonMain.dependencies {
            // CORE
            implementation(projects.core.data)
            implementation(projects.core.domain)
            implementation(projects.core.designsystem)
            implementation(projects.core.presentation)

            implementation(libs.jetbrains.compose.navigation)
            implementation(libs.jetbrains.compose.material.icons.core)
            implementation(libs.jetbrains.compose.material.icons.extended)
            implementation(libs.bundles.koin.common)

            // AUTH FEATURE
            implementation(projects.feature.auth.domain)
            implementation(projects.feature.auth.presentation)

            // CHAT FEATURE
            implementation(projects.feature.chat.data)
            implementation(projects.feature.chat.database)
            implementation(projects.feature.chat.domain)
            implementation(projects.feature.chat.presentation)

            // GLOBAL POSITION
            implementation(projects.feature.globalPosition.data)
            implementation(projects.feature.globalPosition.domain)
            implementation(projects.feature.globalPosition.presentation)

            // ECONOMY FEATURE
            implementation(projects.feature.economy.data)
            implementation(projects.feature.economy.domain)
            implementation(projects.feature.economy.presentation)

            // ONBOARDING FEATURE
            implementation(projects.feature.onboarding.data)
            implementation(projects.feature.onboarding.domain)
            implementation(projects.feature.onboarding.presentation)

            // CLUB FEATURE
            implementation(projects.feature.club.data)
            implementation(projects.feature.club.domain)
            implementation(projects.feature.club.presentation)

            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.jetbrains.compose.viewmodel)
            implementation(libs.jetbrains.lifecycle.compose)

            implementation(libs.koin.core)
        }
    }
}