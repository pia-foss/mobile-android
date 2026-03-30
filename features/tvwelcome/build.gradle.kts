plugins {
    alias(libs.plugins.library)
    alias(libs.plugins.configuration)
    alias(libs.plugins.koin.compiler)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.compose)
}

android {
    namespace = "com.kape.tvwelcome"
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":core:contracts"))
    implementation(project(":capabilities:buildconfig"))
    implementation(project(":capabilities:ui"))

    implementation(libs.bundles.compose)
    androidTestImplementation(libs.bundles.composeandroidtest)
    implementation(libs.bundles.android)
    implementation(libs.bundles.koin)
    testImplementation(libs.bundles.kointest)
    androidTestImplementation(libs.bundles.koinandroidtest)
    implementation(libs.coroutines)
    testImplementation(libs.coroutines.test)
    androidTestImplementation(libs.bundles.coroutines.androidtest)
    testImplementation(libs.bundles.test)
    runtimeOnly(libs.launcher)
}

koinCompiler {
    compileSafety = false
}