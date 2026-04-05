plugins {
    alias(libs.plugins.library)
    alias(libs.plugins.configuration)
    alias(libs.plugins.serialization)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.koin.compiler)
}

android {
    namespace = "com.kape.localprefs.prefs"
}

dependencies {
    implementation(project(":core:data"))
    implementation(project(":core:localprefs:data"))
    implementation(libs.bundles.koin)
    implementation(libs.bundles.serialization)
    implementation(libs.kape.account)
    implementation(libs.crypto)
}