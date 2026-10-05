rootProject.name = "CheckApp"

pluginManagement {
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

// Entry points
include(":androidApp")
include(":shared:app")

// Capas FSD
include(":shared:processes")
include(":shared:pages")
include(":shared:widgets")
include(":shared:features")
include(":shared:entities")
include(":shared:design")
include(":shared:platform")

// Core
include(":shared:core:common")
include(":shared:core:network")
include(":shared:core:database")
include(":shared:core:datastore")
include(":shared:core:sync")
include(":shared:core:connectivity")
include(":shared:core:permissions")
include(":shared:core:logging")

// Reglas de arquitectura (Konsist)
include(":shared:archtest")
