plugins {
    alias(libs.plugins.library)
    alias(libs.plugins.configuration)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.compose)
}

android {
    namespace = "com.kape.rating"

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
    implementation(project(":core:localprefs:rating"))
    implementation(project(":core:localprefs:rating:data"))
    implementation(project(":core:vpnconnect"))
    implementation(project(":capabilities:ui"))

    implementation(libs.kape.vpnmanager)
    implementation(libs.bundles.compose)
    androidTestImplementation(libs.bundles.composeandroidtest)
    implementation(libs.bundles.android)
    implementation(libs.bundles.koin)
    androidTestImplementation(libs.bundles.koinandroidtest)
    implementation(libs.coroutines)
    testImplementation(libs.bundles.kointest)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.bundles.test)
    androidTestImplementation(libs.bundles.coroutines.androidtest)
    runtimeOnly(libs.launcher)
}