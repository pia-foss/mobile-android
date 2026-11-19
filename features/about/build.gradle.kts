plugins {
    alias(libs.plugins.pia.android.library.compose)
}

android {
    namespace = "com.kape.about"
}

dependencies {
    implementation(project(":capabilities:ui"))
    implementation(project(":capabilities:appbar"))
    implementation(libs.coroutines.android)
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.core)
    implementation(libs.koin.annotations)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.bundles.junit5.bundle)
    testRuntimeOnly(libs.bundles.junit5.runtime.bundle)
    testImplementation(libs.coroutines.test)
}