plugins {
    alias(libs.plugins.library)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.serialization)
    alias(libs.plugins.configuration)
}

android {
    namespace = "com.kape.contracts"
}

dependencies {
    implementation(project(":core:data"))
    implementation(libs.coroutines)
    implementation(libs.bundles.serialization)
    implementation(libs.kape.kpi)
    implementation(libs.kape.vpnmanager)
}