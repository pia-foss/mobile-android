plugins {
    alias(libs.plugins.library)
    alias(libs.plugins.kotlin)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.configuration)
}

android {
    namespace = "com.kape.shortcut.prefs"
}

dependencies {
    implementation(project(":core:utils"))
    implementation(libs.bundles.serialization)
}