plugins {
    alias(libs.plugins.library)
    alias(libs.plugins.configuration)
    alias(libs.plugins.koin.compiler)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.junit5)
}

android {
    namespace = "com.kape.vpnconnect"
}

dependencies {
    implementation(project(":core:utils"))
    implementation(project(":core:contracts"))
    implementation(project(":core:data"))
    implementation(project(":core:portforwarding"))
    implementation(project(":core:obfuscator"))
    implementation(project(":core:localprefs:prefs"))
    implementation(project(":core:localprefs:data"))
    implementation(project(":capabilities:shareevents"))
    implementation(project(":capabilities:ui"))
    implementation(libs.material3)
    implementation(libs.kape.vpnmanager)
    implementation(libs.bundles.koin)
    testImplementation(libs.bundles.kointest)
    androidTestImplementation(libs.bundles.koinandroidtest)
    implementation(libs.kape.account)
    implementation(libs.bundles.serialization)
    implementation(libs.coroutines)
    testImplementation(libs.coroutines.test)
    androidTestImplementation(libs.bundles.coroutines.androidtest)
    testImplementation(libs.bundles.test)
    runtimeOnly(libs.launcher)
}

koinCompiler {
    compileSafety = false
}