plugins {
    alias(libs.plugins.pia.android.library)
}

android {
    namespace = "com.kape.router"
}

dependencies {
    api(platform(libs.androidx.compose.bom))
    api(libs.bundles.compose.bundle)
    implementation(libs.coroutines.android)
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.core)
    implementation(libs.koin.annotations)
    implementation(project(":core:contracts"))
}