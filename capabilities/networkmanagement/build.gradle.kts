plugins {
    alias(libs.plugins.library)
    alias(libs.plugins.configuration)
    alias(libs.plugins.koin.compiler)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.serialization)
}

android {
    namespace = "com.kape.networkmanagement"
}

dependencies {
    implementation(project(":core:utils"))
    implementation(project(":core:localprefs:prefs"))
    implementation(project(":core:localprefs:data"))
    implementation(project(":capabilities:ui"))

    implementation(libs.bundles.koin)
    testImplementation(libs.bundles.kointest)
    androidTestImplementation(libs.bundles.koinandroidtest)
    implementation(libs.bundles.serialization)
}

koinCompiler {
    compileSafety = false
}