import Dependencies.KOTLIN_COMPILER_EXTENSION


plugins {
    alias(libs.plugins.library)
    alias(libs.plugins.kotlin)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.junit5)

    alias(libs.plugins.compose)
}

android {
    namespace = "com.kape.dedicatedip"
    compileSdk = 35

    defaultConfig {
        minSdk = 24
    }
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = KOTLIN_COMPILER_EXTENSION
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