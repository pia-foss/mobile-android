plugins {
    alias(libs.plugins.pia.android.library.compose)
}

android {
    namespace = "com.kape.ui"
}

dependencies {
    api(project(":core:contracts"))
    api(project(":core:router"))
    implementation(libs.bundles.android.bundle)
    api(platform(libs.androidx.compose.bom))
    api(libs.bundles.compose.bundle)
    api(platform(libs.koin.bom))
    api(libs.bundles.koin.bundle)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.bundles.junit5.bundle)
    testRuntimeOnly(libs.bundles.junit5.runtime.bundle)
    testImplementation(libs.mockk)
}