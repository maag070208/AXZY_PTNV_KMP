import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    iosArm64()
    iosSimulatorArm64()

    android {
        namespace = "com.axzydev.checkapp.platform"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        compilerOptions { jvmTarget = JvmTarget.JVM_11 }
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":shared:core:common"))
            api(project(":shared:core:connectivity"))
            api(project(":shared:core:permissions"))
            implementation(project(":shared:core:datastore"))
            implementation(project(":shared:core:database"))
            implementation(project(":shared:core:network"))
            implementation(project(":shared:core:sync"))
            implementation(libs.okio)
            implementation(libs.koin.core)

            // UI compartida (vistas interop de cámara)
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
        }
        androidMain.dependencies {
            implementation(libs.koin.android)
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.core.ktx)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.androidx.camera.core)
            implementation(libs.androidx.camera.camera2)
            implementation(libs.androidx.camera.lifecycle)
            implementation(libs.androidx.camera.view)
            implementation(libs.mlkit.barcode.scanning)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}
