import Dependencies.implementCompose
import Dependencies.implementComposeNavigation
import Dependencies.implementFeatureModule
import Dependencies.implementKoin
import Dependencies.implementKtor
import Dependencies.implementSerialization
import Dependencies.implementViewModel

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.kape.splash"
    compileSdk = 33

    defaultConfig {
        minSdk = 21
    }

    flavorDimensions.add("provider")
    productFlavors {
        create("amazon") {
            dimension = "provider"
        }
        create("google") {
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
        kotlinCompilerExtensionVersion = "1.5.1"
    }
}

dependencies {

    implementation(project(":core:router"))
    implementation(project(":core:payments"))
    implementation(project(":capabilities:ui"))

    implementFeatureModule()
}