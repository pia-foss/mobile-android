plugins {
    alias(libs.plugins.library)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.junit5)
    alias(libs.plugins.configuration)
    alias(libs.plugins.koin.compiler)
}

android {
    namespace = "com.kape.shareevents"

    compileOptions {
    }
}

dependencies {
    coreLibraryDesugaring(libs.desugar)

    implementation(libs.kape.kpi)
    implementation(project(":core:contracts"))
    implementation(project(":core:data"))
    implementation(project(":core:localprefs:prefs"))
    implementation(project(":core:localprefs:data"))

    implementation(libs.bundles.serialization)
    implementation(libs.bundles.koin)
    implementation(libs.coroutines)
    testImplementation(libs.bundles.test)
    testImplementation(libs.bundles.kointest)
    testImplementation(libs.coroutines.test)
    runtimeOnly(libs.launcher)
}

koinCompiler {
    compileSafety = false
}