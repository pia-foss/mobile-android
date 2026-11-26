plugins {
    alias(libs.plugins.pia.android.feature)
}

android {
    namespace = "com.kape.vpnregionselection"
}

dependencies {
    implementation(project(":core:regions"))
    implementation(project(":core:localprefs:prefs"))
    implementation(project(":capabilities:appbar"))

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.bundles.junit5.bundle)
    testRuntimeOnly(libs.bundles.junit5.runtime.bundle)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.mockk)
}