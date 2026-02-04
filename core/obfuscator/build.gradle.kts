plugins {
    alias(libs.plugins.library)
    alias(libs.plugins.configuration)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.junit5)
}

android {
    namespace = "com.kape.obfuscator.binder"
}

dependencies {
    implementation(libs.kape.obfuscator)
    implementation(libs.coroutines)
    testImplementation(libs.coroutines.test)
    androidTestImplementation(libs.bundles.coroutines.androidtest)
    implementation(libs.bundles.koin)
    testImplementation(libs.bundles.kointest)
    androidTestImplementation(libs.bundles.koinandroidtest)
    testImplementation(libs.bundles.test)
    runtimeOnly(libs.launcher)
}