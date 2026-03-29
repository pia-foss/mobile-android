plugins {
    alias(libs.plugins.library)
    alias(libs.plugins.configuration)
    alias(libs.plugins.serialization)
    alias(libs.plugins.ktlint)
}

android {
    namespace = "com.kape.localprefs.data"
}

dependencies {
    implementation(libs.bundles.serialization)
    implementation(libs.kape.account)
}