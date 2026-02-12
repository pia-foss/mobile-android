plugins {
    alias(libs.plugins.library)
    alias(libs.plugins.configuration)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.junit5)
    alias(libs.plugins.compose)
}

android {
    namespace = "com.kape.dedicatedip"
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
        create("meta") {
            dimension = "provider"
        }
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":core:utils"))
    implementation(project(":core:router"))
    implementation(project(":core:regions"))
    implementation(project(":core:payments"))
    implementation(project(":core:vpnconnect"))
    implementation(project(":core:localprefs:dip"))
    implementation(project(":core:localprefs:payments:data"))
    implementation(project(":core:localprefs:payments"))
    implementation(project(":core:localprefs:connection"))
    implementation(project(":features:appbar"))
    implementation(project(":capabilities:ui"))
    implementation(project(":capabilities:buildconfig"))
    implementation(libs.kape.account)
    implementation(libs.kape.regions)
    implementation(libs.bundles.compose)
    androidTestImplementation(libs.bundles.composeandroidtest)
    implementation(libs.bundles.android)
    implementation(libs.bundles.koin)
    testImplementation(libs.bundles.kointest)
    androidTestImplementation(libs.bundles.koinandroidtest)
    implementation(libs.coroutines)
    testImplementation(libs.coroutines.test)
    androidTestImplementation(libs.bundles.coroutines.androidtest)
    testImplementation(libs.bundles.test)
    runtimeOnly(libs.launcher)
    implementation(libs.constraintlayout)
    implementation(libs.kape.vpnmanager)
}