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
        namespace = "com.axzydev.checkapp.design"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        compilerOptions { jvmTarget = JvmTarget.JVM_11 }
        // Sin esto, `composeResources` de este módulo NO se empaqueta: la
        // librería compila, pero al abrir la app el recurso no existe en el APK
        // y salta `MissingResourceException`.
        androidResources { enable = true }
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":shared:core:common"))
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.qrcode.kotlin)
        }
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

compose.resources {
    publicResClass = true
    packageOfResClass = "com.axzydev.checkapp.design.generated.resources"
    generateResClass = always
}
