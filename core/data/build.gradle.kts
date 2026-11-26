plugins {
    alias(libs.plugins.pia.android.library)
}

android {
    namespace = "com.kape.data"
}

dependencies {
    implementation(libs.coroutines.android)
    implementation(libs.bundles.serialization.bundle)
    api(libs.navigation3.runtime)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.bundles.junit5.bundle)
    testRuntimeOnly(libs.bundles.junit5.runtime.bundle)
}