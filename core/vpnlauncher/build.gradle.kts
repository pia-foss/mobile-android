plugins {
    alias(libs.plugins.pia.android.library)
}

android {
    namespace = "com.kape.vpnlauncher"
}

dependencies {
    implementation(project(":core:contracts"))

    implementation(platform(libs.koin.bom))
    implementation(libs.koin.core)
    implementation(libs.koin.annotations)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.bundles.junit5.bundle)
    testRuntimeOnly(libs.bundles.junit5.runtime.bundle)
    testImplementation(libs.coroutines.test)
}