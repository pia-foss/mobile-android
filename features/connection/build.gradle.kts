plugins {
    alias(libs.plugins.pia.android.feature)
}

android {
    namespace = "com.kape.connection"
}

dependencies {
    implementation(project(":core:regions"))
    implementation(project(":core:localprefs:prefs"))
    implementation(project(":core:vpnconnect"))
    implementation(project(":capabilities:snooze"))
    implementation(project(":capabilities:sharedui"))
    implementation(project(":capabilities:appbar"))
    implementation(project(":capabilities:rating"))
    implementation(libs.androidx.constraintlayout.compose)
    implementation(libs.reorderable)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.bundles.junit5.bundle)
    testRuntimeOnly(libs.bundles.junit5.runtime.bundle)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.mockk)
}