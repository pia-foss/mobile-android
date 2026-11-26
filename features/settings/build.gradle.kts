plugins {
    alias(libs.plugins.pia.android.feature)
    alias(libs.plugins.pia.android.flavors)
}

android {
    namespace = "com.kape.settings"
}

dependencies {
    implementation(project(":core:regions"))
    implementation(project(":core:localprefs:prefs"))
    implementation(project(":core:vpnconnect"))
    implementation(project(":capabilities:csi"))
    implementation(project(":capabilities:location"))
    implementation(project(":capabilities:appbar"))
    implementation(libs.androidx.constraintlayout.compose)
    implementation(libs.accompanist.drawablepainter)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.bundles.junit5.bundle)
    testRuntimeOnly(libs.bundles.junit5.runtime.bundle)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.mockk)
}