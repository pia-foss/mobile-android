plugins {
    alias(libs.plugins.library)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.configuration)
    alias(libs.plugins.koin.compiler)
    alias(libs.plugins.serialization)
}

android {
    namespace = "com.kape.router"
}

dependencies {
    implementation(project(":core:contracts"))
    implementation(project(":core:data"))
    implementation(libs.coroutines)
    implementation(libs.serialization.json)
    implementation(libs.bundles.compose)
    implementation(libs.bundles.koin)
    testImplementation(libs.coroutines.test)
    androidTestImplementation(libs.bundles.coroutines.androidtest)
}

koinCompiler {
    compileSafety = false
}