plugins {
    alias(libs.plugins.pia.android.library)
}

android {
    // Not "com.kape.account": the mobile-shared-account library already uses that namespace.
    namespace = "com.kape.account.core"
}

dependencies {
    implementation(project(":core:contracts"))
    api(libs.mobile.shared.account)
    implementation(libs.coroutines.android)

    implementation(platform(libs.koin.bom))
    implementation(libs.koin.core)
    implementation(libs.koin.annotations)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.bundles.junit5.bundle)
    testRuntimeOnly(libs.bundles.junit5.runtime.bundle)
    testImplementation(libs.coroutines.test)
}