plugins {
    alias(libs.plugins.library)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.junit5)
    alias(libs.plugins.configuration)
    alias(libs.plugins.koin.compiler)
}

android {
    namespace = "com.kape.featureflags"
}

dependencies {
    coreLibraryDesugaring(libs.desugar)

    implementation(libs.kape.account)
    implementation(project(":core:utils"))

    implementation(libs.bundles.serialization)
    implementation(libs.bundles.koin)
    testImplementation(libs.bundles.kointest)
    androidTestImplementation(libs.bundles.koinandroidtest)
}

koinCompiler {
    compileSafety = false
}