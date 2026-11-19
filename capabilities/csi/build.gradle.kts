plugins {
    alias(libs.plugins.pia.android.library)
}

android {
    // Not "com.kape.csi": the mobile-shared-csi library may already use that namespace.
    namespace = "com.kape.csi.core"
}

dependencies {
    implementation(project(":core:contracts"))
    implementation(project(":core:localprefs:prefs"))
    implementation(libs.mobile.shared.csi)
    implementation(libs.coroutines.android)

    implementation(platform(libs.koin.bom))
    implementation(libs.koin.core)
    implementation(libs.koin.annotations)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.bundles.junit5.bundle)
    testRuntimeOnly(libs.bundles.junit5.runtime.bundle)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.mockk)
}