plugins {
    alias(libs.plugins.library)
    alias(libs.plugins.configuration)
    alias(libs.plugins.kotlin)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.serialization)
}

android {
    namespace = "com.kape.dip.prefs"
}

dependencies {
    implementation(project(":core:utils"))
    implementation(project(":capabilities:buildconfig"))
    implementation(libs.kape.account)
    implementation(libs.bundles.serialization)
}