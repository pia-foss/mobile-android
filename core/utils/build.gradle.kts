plugins {
    alias(libs.plugins.library)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.serialization)
    alias(libs.plugins.configuration)
    alias(libs.plugins.koin.compiler)
}

android {
    namespace = "com.kape.utils"
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
    }
}

dependencies {
    coreLibraryDesugaring(libs.desugar)
    implementation(project(":core:contracts"))
    implementation(libs.crypto)
    implementation(libs.coroutines)
    implementation(libs.kape.vpnmanager)
    implementation(libs.bundles.serialization)
    implementation(libs.bundles.koin)
}

koinCompiler {
    compileSafety = false
}