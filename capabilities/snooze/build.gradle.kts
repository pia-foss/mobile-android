plugins {
    alias(libs.plugins.library)
    alias(libs.plugins.configuration)
    alias(libs.plugins.koin.compiler)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.junit5)
}

android {
    namespace = "com.kape.snooze"
}

dependencies {
    implementation(libs.bundles.koin)
    testImplementation(libs.bundles.kointest)
    androidTestImplementation(libs.bundles.koinandroidtest)
    implementation(libs.bundles.android)

    implementation(project(":core:data"))
    implementation(project(":core:localprefs:prefs"))
    implementation(project(":core:vpnlauncher"))
}

koinCompiler {
    compileSafety = false
}