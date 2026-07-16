plugins {
    alias(libs.plugins.library)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.serialization)
    alias(libs.plugins.configuration)
}

android {
    namespace = "com.kape.data"
}

dependencies {
    implementation(libs.coroutines)
    implementation(libs.bundles.serialization)
}