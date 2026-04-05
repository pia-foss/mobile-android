plugins {
    alias(libs.plugins.library)
    alias(libs.plugins.configuration)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.compose)
}

android {
    namespace = "com.kape.rating"

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":core:data"))
    implementation(project(":core:utils"))
    implementation(project(":core:localprefs:prefs"))
    implementation(project(":core:localprefs:data"))
    implementation(project(":core:vpnconnect"))
    implementation(project(":capabilities:ui"))

    implementation(libs.kape.vpnmanager)
    implementation(libs.bundles.compose)
    androidTestImplementation(libs.bundles.composeandroidtest)
    implementation(libs.bundles.android)
    implementation(libs.bundles.koin)
    androidTestImplementation(libs.bundles.koinandroidtest)
    implementation(libs.coroutines)
    testImplementation(libs.bundles.kointest)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.bundles.test)
    androidTestImplementation(libs.bundles.coroutines.androidtest)
    runtimeOnly(libs.launcher)
}