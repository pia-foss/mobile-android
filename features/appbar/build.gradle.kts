plugins {
    alias(libs.plugins.library)
    alias(libs.plugins.configuration)
    alias(libs.plugins.koin.compiler)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.compose)
}

android {
    namespace = "com.kape.appbar"
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":capabilities:ui"))
    implementation(project(":core:vpnconnect"))
    implementation(project(":core:utils"))
    implementation(project(":core:contracts"))
    implementation(libs.bundles.compose)
    androidTestImplementation(libs.bundles.composeandroidtest)
    implementation(libs.viewmodel)
    implementation(libs.bundles.koin)
    testImplementation(libs.bundles.kointest)
    androidTestImplementation(libs.bundles.koinandroidtest)
    implementation(libs.kape.vpnmanager)
}

koinCompiler {
    compileSafety = false
}