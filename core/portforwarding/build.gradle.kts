plugins {
    alias(libs.plugins.library)
    alias(libs.plugins.configuration)
    alias(libs.plugins.koin.compiler)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.serialization)
}

android {
    namespace = "com.kape.portforwarding"
}

dependencies {
    implementation(project(":core:data"))
    implementation(project(":core:httpclient"))
    implementation(project(":core:localprefs:prefs"))
    implementation(project(":core:localprefs:data"))
    implementation(libs.coroutines)
    implementation(libs.bundles.serialization)
    implementation(libs.bundles.ktor)
    implementation(libs.bundles.koin)
    testImplementation(libs.bundles.kointest)
    androidTestImplementation(libs.bundles.koinandroidtest)
}

koinCompiler {
    compileSafety = false
}