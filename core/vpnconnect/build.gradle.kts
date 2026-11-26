plugins {
    alias(libs.plugins.pia.android.library)
}

android {
    namespace = "com.kape.vpnconnect"

    testOptions.unitTests.isReturnDefaultValues = true
}

dependencies {
    implementation(project(":core:network"))
    implementation(project(":core:contracts"))
    implementation(project(":core:account"))
    implementation(project(":core:regions"))
    implementation(project(":core:localprefs:prefs"))
    implementation(project(":core:obfuscator"))
    implementation(project(":core:portforwarding"))
    implementation(project(":capabilities:ui"))

    implementation(libs.kape.platform.sdk)
    implementation(libs.bundles.serialization.bundle)
    implementation(libs.coroutines.android)
    implementation(libs.bundles.ktor.bundle)
    implementation(libs.androidx.core.ktx)

    implementation(platform(libs.koin.bom))
    implementation(libs.koin.core)
    implementation(libs.koin.android)
    implementation(libs.koin.annotations)
    implementation(libs.koin.androidx.workmanager)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.bundles.junit5.bundle)
    testRuntimeOnly(libs.bundles.junit5.runtime.bundle)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.mockk)
}