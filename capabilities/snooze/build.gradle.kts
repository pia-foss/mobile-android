plugins {
    alias(libs.plugins.library)
    alias(libs.plugins.configuration)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.junit5)
}

android {
    namespace = "com.kape.snooze"
    flavorDimensions.add("provider")
    productFlavors {
        create("amazon") {
            dimension = "provider"
        }
        create("google") {
            dimension = "provider"
        }
        create("noinapp") {
            dimension = "provider"
        }
    }
}

dependencies {
    implementation(libs.bundles.koin)
    testImplementation(libs.bundles.kointest)
    androidTestImplementation(libs.bundles.koinandroidtest)
    implementation(libs.bundles.android)

    implementation(project(":core:utils"))
    implementation(project(":core:localprefs:connection"))
    implementation(project(":core:vpnlauncher"))
}