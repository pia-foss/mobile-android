plugins {
    alias(libs.plugins.pia.android.library)
}

android {
    namespace = "com.kape.shareevents"
}

dependencies {
    implementation(project(":core:contracts"))
    implementation(project(":core:account"))
    implementation(project(":core:localprefs:prefs"))
    implementation(libs.mobile.shared.kpi)
    implementation(libs.bundles.serialization.bundle)
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