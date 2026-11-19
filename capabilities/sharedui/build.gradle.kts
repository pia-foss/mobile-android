plugins {
    alias(libs.plugins.pia.android.library.compose)
}

android {
    namespace = "com.kape.sharedui"
}

dependencies {
    implementation(project(":core:contracts"))
    implementation(project(":capabilities:ui"))
    implementation(libs.bundles.android.bundle)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.bundles.junit5.bundle)
    testRuntimeOnly(libs.bundles.junit5.runtime.bundle)
    testImplementation(libs.coroutines.test)
}