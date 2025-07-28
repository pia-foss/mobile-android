plugins {
    alias(libs.plugins.library)
    alias(libs.plugins.kotlin)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.serialization)
    alias(libs.plugins.configuration)
}

android {
    namespace = "com.kape.csi.prefs"
}

dependencies {
    implementation(project(":core:utils"))
    implementation(libs.bundles.serialization)
}