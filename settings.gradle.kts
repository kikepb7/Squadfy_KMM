rootProject.name = "Squadfy_App"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
}

include(":composeApp")
include(":core:presentation")
include(":core:domain")
include(":core:data")
include(":core:designsystem")
include(":feature:auth:presentation")
include(":feature:auth:domain")
include(":feature:chat:presentation")
include(":feature:chat:domain")
include(":feature:chat:data")
include(":feature:chat:database")
include(":feature:globalPosition:data")
include(":feature:globalPosition:domain")
include(":feature:globalPosition:presentation")
include(":feature:economy:data")
include(":feature:economy:domain")
include(":feature:economy:presentation")
include(":feature:onboarding:data")
include(":feature:onboarding:domain")
include(":feature:onboarding:presentation")
include(":feature:club:database")
include(":feature:club:data")
include(":feature:club:domain")
include(":feature:club:presentation")
