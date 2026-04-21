plugins {
    alias(libs.plugins.library)
    alias(libs.plugins.configuration)
    alias(libs.plugins.koin.compiler)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.compose)
}

android {
    namespace = "com.kape.splash"

    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation(project(":core:contracts"))
    implementation(project(":core:data"))
    implementation(project(":core:regions"))
    implementation(project(":core:httpclient"))
    implementation(project(":core:vpnconnect"))
    implementation(project(":core:utils"))
    implementation(project(":capabilities:ui"))
    implementation(project(":capabilities:notifications"))
    implementation(project(":capabilities:featureflags"))

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