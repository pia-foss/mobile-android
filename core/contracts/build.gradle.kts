plugins {
    alias(libs.plugins.library)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.serialization)
    alias(libs.plugins.configuration)
}

android {
    namespace = "com.kape.contracts"
}

dependencies {
    implementation(project(":core:data"))
    implementation(project(":core:localprefs:data"))
    implementation(libs.kape.account)
    implementation(libs.coroutines)
    implementation(libs.bundles.serialization)
    implementation(libs.mobile.android.vpn.manager)
    implementation(libs.compose.ui.graphics)
    implementation(libs.material3)
}