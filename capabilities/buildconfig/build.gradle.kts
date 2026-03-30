plugins {
    alias(libs.plugins.library)
    alias(libs.plugins.configuration)
    alias(libs.plugins.ktlint)
}

android {
    namespace = "com.kape.buildconfig"
}

dependencies {
    implementation(project(":core:contracts"))
    implementation(libs.bundles.koin)
    testImplementation(libs.bundles.kointest)
    androidTestImplementation(libs.bundles.koinandroidtest)
}