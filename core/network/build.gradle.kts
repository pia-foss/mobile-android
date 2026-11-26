plugins {
    alias(libs.plugins.pia.android.library)
}

android {
    namespace = "com.kape.network"
}

dependencies {
    implementation(project(":core:contracts"))
    // Exposed: PinnedHttpClientFactory hands out Ktor clients.
    api(libs.bundles.ktor.bundle)
    implementation(libs.spongycastle)

    implementation(platform(libs.koin.bom))
    implementation(libs.koin.core)
    implementation(libs.koin.annotations)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.bundles.junit5.bundle)
    testRuntimeOnly(libs.bundles.junit5.runtime.bundle)
}