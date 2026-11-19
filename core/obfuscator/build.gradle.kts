plugins {
    alias(libs.plugins.pia.android.library)
}

android {
    namespace = "com.kape.obfuscator.core"
}

dependencies {
    implementation(libs.mobile.android.obfuscation.proxy)
    implementation(libs.coroutines.android)

    implementation(platform(libs.koin.bom))
    implementation(libs.koin.core)
    implementation(libs.koin.annotations)
}