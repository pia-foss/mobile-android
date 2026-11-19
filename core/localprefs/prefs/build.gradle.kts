plugins {
    alias(libs.plugins.pia.android.library)
}

android {
    namespace = "com.kape.localprefs.prefs"
}

dependencies {
    implementation(project(":core:contracts"))
    api(project(":core:localprefs:api"))
    api(project(":core:localprefs:data"))
    api(libs.datastore.preferences)

    implementation(libs.bundles.serialization.bundle)
    implementation(libs.coroutines.android)
    implementation(libs.security.crypto)
    implementation(libs.tink.android)

    implementation(platform(libs.koin.bom))
    implementation(libs.koin.core)
    implementation(libs.koin.annotations)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.bundles.junit5.bundle)
    testRuntimeOnly(libs.bundles.junit5.runtime.bundle)
}