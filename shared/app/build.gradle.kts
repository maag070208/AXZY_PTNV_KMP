import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }

    android {
        namespace = "com.axzydev.checkapp.app"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
        androidResources {
            enable = true
        }
        withHostTest {
            isIncludeAndroidResources = true
        }
        withDeviceTestBuilder {
            sourceSetTreeName = "test"
        }.configure {
            instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.compose.uiTooling)
        }
        commonMain.dependencies {
            // Capas FSD
            implementation(project(":shared:processes"))
            implementation(project(":shared:pages"))
            implementation(project(":shared:widgets"))
            implementation(project(":shared:features"))
            implementation(project(":shared:entities"))
            implementation(project(":shared:design"))
            implementation(project(":shared:platform"))

            // Core
            api(project(":shared:core:common"))
            implementation(project(":shared:core:network"))
            implementation(project(":shared:core:database"))
            implementation(project(":shared:core:datastore"))
            implementation(project(":shared:core:sync"))
            implementation(project(":shared:core:connectivity"))
            implementation(project(":shared:core:permissions"))
            implementation(project(":shared:core:logging"))

            // Compose / lifecycle
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)

            // DI
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)

            // Navegación
            implementation(libs.androidx.navigation.compose)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

compose.resources {
    packageOfResClass = "com.axzydev.checkapp.app.generated.resources"
}

dependencies {
    androidRuntimeClasspath(libs.compose.uiTooling)
}
