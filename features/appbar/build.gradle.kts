import Dependencies.KOTLIN_COMPILER_EXTENSION
import Dependencies.implementCompose
import Dependencies.implementComposeTv
import Dependencies.implementKoin
import Dependencies.implementViewModel
import Dependencies.implementVpnManager

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("org.jlleitschuh.gradle.ktlint")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.kape.appbar"
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
    implementation(project(":capabilities:ui"))
    implementation(project(":core:vpnconnect"))
    implementation(project(":core:utils"))
    implementCompose()
    implementComposeTv()
    implementViewModel()
    implementKoin()
    implementVpnManager()
}