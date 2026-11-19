plugins {
    alias(libs.plugins.pia.android.library.compose)
}

android {
    namespace = "com.kape.rating"
}

dependencies {
    implementation(project(":core:contracts"))
    implementation(project(":core:localprefs:prefs"))
    implementation(project(":capabilities:ui"))
    implementation(libs.coroutines.android)

    implementation(platform(libs.koin.bom))
    implementation(libs.koin.core)
    implementation(libs.koin.annotations)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.bundles.junit5.bundle)
    testRuntimeOnly(libs.bundles.junit5.runtime.bundle)
    testImplementation(libs.coroutines.test)
}