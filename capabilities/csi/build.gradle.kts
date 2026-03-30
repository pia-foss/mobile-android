plugins {
    alias(libs.plugins.library)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.junit5)
    alias(libs.plugins.configuration)
    alias(libs.plugins.koin.compiler)
}

android {
    namespace = "com.kape.csi"
}

dependencies {
    implementation(libs.kape.csi)
    implementation(libs.bundles.koin)
    implementation(libs.coroutines)
    testImplementation(libs.bundles.test)
    testImplementation(libs.bundles.kointest)
    testImplementation(libs.coroutines.test)
    runtimeOnly(libs.launcher)
}

koinCompiler {
    compileSafety = false
}