plugins {
    alias(libs.plugins.kotlinJvm)
}

dependencies {
    testImplementation(libs.konsist)
    testImplementation(libs.kotlin.testJunit)
    testImplementation(libs.junit)
}
