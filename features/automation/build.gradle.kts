plugins {
    alias(libs.plugins.pia.android.feature)
    alias(libs.plugins.pia.android.flavors)
}

android {
    namespace = "com.kape.automation"
}

dependencies {
    implementation(project(":core:localprefs:prefs"))
    implementation(project(":core:vpnconnect"))
    implementation(project(":core:vpnlauncher"))
    implementation(project(":capabilities:location"))
    implementation(project(":capabilities:networkmanagement"))
    implementation(project(":capabilities:appbar"))

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.bundles.junit5.bundle)
    testRuntimeOnly(libs.bundles.junit5.runtime.bundle)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.mockk)
}