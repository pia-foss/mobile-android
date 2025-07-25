plugins {
    alias(libs.plugins.library)
    alias(libs.plugins.configuration)
    alias(libs.plugins.kotlin)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.serialization)
}

android {
    namespace = "com.kape.networkmanagement"
}

dependencies {
    implementation(project(":core:localprefs:networkmanagement:data"))
    implementation(project(":core:localprefs:networkmanagement"))
    implementation(project(":core:utils"))
    implementation(project(":capabilities:ui"))

    implementation(libs.bundles.koin)
    testImplementation(libs.bundles.kointest)
    androidTestImplementation(libs.bundles.koinandroidtest)
    implementation(libs.bundles.serialization)
}