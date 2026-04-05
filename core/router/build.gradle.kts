plugins {
    alias(libs.plugins.library)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.configuration)
    alias(libs.plugins.koin.compiler)
}

android {
    namespace = "com.kape.router"
}

dependencies {
    implementation(project(":core:contracts"))
    implementation(project(":core:data"))
    implementation(libs.coroutines)
    implementation(libs.bundles.compose)
    implementation(libs.bundles.koin)
    testImplementation(libs.coroutines.test)
    androidTestImplementation(libs.bundles.coroutines.androidtest)
}

koinCompiler {
    compileSafety = false
}