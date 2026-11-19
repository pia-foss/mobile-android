plugins {
    alias(libs.plugins.pia.android.feature)
}

android {
    namespace = "com.kape.widget"
}

dependencies {
    implementation(project(":core:vpnlauncher"))
    implementation(libs.bundles.glance.bundle)
}