plugins {
    alias(libs.plugins.pia.android.feature)
}

android {
    namespace = "com.kape.profile"
}

dependencies {
    implementation(project(":core:account"))
    implementation(project(":capabilities:appbar"))

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.bundles.junit5.bundle)
    testRuntimeOnly(libs.bundles.junit5.runtime.bundle)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.mockk)
}