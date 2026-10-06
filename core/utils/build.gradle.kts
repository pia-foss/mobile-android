plugins {
    alias(libs.plugins.library)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.serialization)
    alias(libs.plugins.configuration)
    alias(libs.plugins.koin.compiler)
    alias(libs.plugins.junit5)
}

android {
    namespace = "com.kape.utils"
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
    }
    testOptions {
        unitTests {
            isReturnDefaultValues = true
        }
    }
}

dependencies {
    coreLibraryDesugaring(libs.desugar)
    implementation(project(":core:contracts"))
    implementation(project(":core:data"))
    implementation(project(":capabilities:buildconfig"))
    implementation(project(":capabilities:notifications"))
    implementation(libs.mobile.shared.account)
    implementation(libs.coroutines)
    implementation(libs.bundles.serialization)
    implementation(libs.bundles.koin)
    testImplementation(libs.bundles.kointest)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.bundles.test)
    runtimeOnly(libs.launcher)
}

koinCompiler {
    compileSafety = false
}