plugins {
    alias(libs.plugins.pia.android.library)
}

android {
    namespace = "com.kape.localprefs.data"
}

dependencies {
    implementation(libs.bundles.serialization.bundle)
    api(libs.mobile.shared.account)
}