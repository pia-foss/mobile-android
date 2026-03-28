plugins {
    alias(libs.plugins.library)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.configuration)
    alias(libs.plugins.koin.compiler)
}

android {
    namespace = "com.kape.location"
}

dependencies {
    implementation(libs.bundles.koin)
    testImplementation(libs.bundles.kointest)
    androidTestImplementation(libs.bundles.koinandroidtest)
}

koinCompiler {
    compileSafety = false
}