plugins {
    alias(libs.plugins.library)
    alias(libs.plugins.serialization)
    alias(libs.plugins.ktlint)
}

android {
    namespace = "com.kape.networkmanagement.data"
}

dependencies {
    implementation(libs.bundles.serialization)
}