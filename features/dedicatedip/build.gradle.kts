import Dependencies.KOTLIN_COMPILER_EXTENSION
import Dependencies.implementAccount
import Dependencies.implementConstraintLayout
import Dependencies.implementFeatureModule
import Dependencies.implementRegions
import Dependencies.implementVpnManager

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("org.jlleitschuh.gradle.ktlint")
    id("de.mannodermaus.android-junit5") version "1.12.0.0"
    id("org.jetbrains.kotlinx.kover")
    id("org.jetbrains.kotlin.plugin.compose")
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
    implementAccount()
    implementRegions()
    implementFeatureModule()
    implementConstraintLayout()
    implementVpnManager()
}