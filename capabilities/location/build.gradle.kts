plugins {
    alias(libs.plugins.library)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.configuration)
    alias(libs.plugins.koin.compiler)
    alias(libs.plugins.junit5)
}

android {
    namespace = "com.kape.location"
    testOptions {
        unitTests {
            isReturnDefaultValues = true
        }
    }
}

dependencies {
    implementation(libs.bundles.koin)
    testImplementation(libs.bundles.kointest)
    androidTestImplementation(libs.bundles.koinandroidtest)
    implementation(libs.coroutines)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.bundles.test)
    runtimeOnly(libs.launcher)
}

koinCompiler {
    compileSafety = false
}