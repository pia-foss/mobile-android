plugins {
    alias(libs.plugins.library)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.configuration)
}

android {
    namespace = "com.kape.shareevents.prefs"
}

dependencies {
    implementation(project(":core:utils"))
}