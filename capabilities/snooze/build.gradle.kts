plugins {
    alias(libs.plugins.pia.android.library)
}

android {
    namespace = "com.kape.snooze"
}

dependencies {
    implementation(project(":core:contracts"))
    implementation(project(":core:localprefs:prefs"))
    implementation(project(":core:vpnlauncher"))
    implementation(libs.coroutines.android)
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.workmanager)

    implementation(platform(libs.koin.bom))
    implementation(libs.koin.core)
    implementation(libs.koin.annotations)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.bundles.junit5.bundle)
    testRuntimeOnly(libs.bundles.junit5.runtime.bundle)
    testImplementation(libs.coroutines.test)
}