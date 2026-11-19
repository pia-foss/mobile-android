plugins {
    alias(libs.plugins.pia.android.library.compose)
}

android {
    namespace = "com.kape.inappbrowser"
}

dependencies {
    implementation(project(":capabilities:ui"))
    implementation(project(":capabilities:appbar"))

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.bundles.junit5.bundle)
    testRuntimeOnly(libs.bundles.junit5.runtime.bundle)
    testImplementation(libs.coroutines.test)
}