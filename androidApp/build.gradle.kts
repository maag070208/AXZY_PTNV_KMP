import java.util.Properties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

/**
 * La key de Google Maps se lee de `local.properties`, que está ignorado por git: una
 * key en el repositorio es una key filtrada, y además cada máquina puede tener la
 * suya. Si falta, se avisa y se compila igual —el mapa saldrá vacío, pero la app
 * arranca—.
 */
val googleMapsApiKey: String = run {
    val file = rootProject.file("local.properties")
    val properties = Properties().apply {
        if (file.exists()) file.inputStream().use { load(it) }
    }
    properties.getProperty("GOOGLE_MAPS_API_KEY").orEmpty().also { key ->
        if (key.isBlank()) {
            logger.warn(
                "GOOGLE_MAPS_API_KEY no está en local.properties: el mapa se verá vacío. " +
                    "Añádela con GOOGLE_MAPS_API_KEY=...",
            )
        }
    }
}

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
}
dependencies {
    implementation(project(":shared:app"))

    implementation(libs.androidx.activity.compose)
    implementation(libs.koin.android)

    implementation(libs.compose.uiToolingPreview)
    debugImplementation(libs.compose.uiTooling)
}

android {
    namespace = "com.axzydev.checkapp"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.axzydev.checkapp"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
        manifestPlaceholders["googleMapsApiKey"] = googleMapsApiKey
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}