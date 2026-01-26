plugins {
    alias(libs.plugins.library)
    alias(libs.plugins.serialization)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.configuration)
}

android {
    namespace = "com.kape.httpclient"
}

dependencies {
    implementation(libs.coroutines)
    testImplementation(libs.coroutines.test)
    androidTestImplementation(libs.bundles.coroutines.androidtest)
    implementation(libs.bundles.ktor)
    implementation(libs.spongycastle)
    implementation(libs.bundles.serialization)
}