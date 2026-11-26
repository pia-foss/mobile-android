plugins {
    alias(libs.plugins.pia.android.library)
}

android {
    // Not "com.kape.regions": the mobile-shared-regions library already uses that namespace.
    namespace = "com.kape.regions.core"
}

dependencies {
    implementation(project(":core:contracts"))
    implementation(libs.bundles.serialization.bundle)
    implementation(libs.coroutines.android)
    implementation(project(":core:localprefs:prefs"))
    implementation(libs.mobile.shared.regions)

    implementation(platform(libs.koin.bom))
    implementation(libs.koin.core)
    implementation(libs.koin.annotations)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.bundles.junit5.bundle)
    testRuntimeOnly(libs.bundles.junit5.runtime.bundle)
    testImplementation(libs.coroutines.test)
}