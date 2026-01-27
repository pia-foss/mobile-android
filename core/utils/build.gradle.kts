plugins {
    alias(libs.plugins.library)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.serialization)
    alias(libs.plugins.configuration)
}

android {
    namespace = "com.kape.utils"
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
    }
}

dependencies {
    coreLibraryDesugaring(libs.desugar)
    implementation(libs.crypto)
    implementation(libs.coroutines)
    implementation(libs.kape.vpnmanager)
    implementation(libs.bundles.serialization)
}