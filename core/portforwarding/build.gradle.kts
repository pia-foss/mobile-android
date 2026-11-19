plugins {
    alias(libs.plugins.pia.android.library)
}

android {
    namespace = "com.kape.portforwarding"
}

dependencies {
    implementation(project(":core:network"))
    implementation(project(":core:contracts"))
    implementation(project(":core:localprefs:prefs"))
    implementation(libs.bundles.serialization.bundle)
    implementation(libs.coroutines.android)
    implementation(libs.androidx.core.ktx)

    implementation(platform(libs.koin.bom))
    implementation(libs.koin.core)
    implementation(libs.koin.annotations)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.bundles.junit5.bundle)
    testRuntimeOnly(libs.bundles.junit5.runtime.bundle)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.mockk)
}