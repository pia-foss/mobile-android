plugins {
    alias(libs.plugins.library)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.configuration)
    alias(libs.plugins.serialization)
}

android {
    namespace = "com.kape.router"
}

dependencies {
    implementation(libs.coroutines)
    implementation(libs.serialization.json)
    implementation(libs.bundles.compose)
    testImplementation(libs.coroutines.test)
    androidTestImplementation(libs.bundles.coroutines.androidtest)
}