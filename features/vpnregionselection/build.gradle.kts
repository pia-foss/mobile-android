import Dependencies.KOTLIN_COMPILER_EXTENSION
import Dependencies.desugarJdkLibs
import Dependencies.implementFeatureModule
import Dependencies.implementSerialization
import Dependencies.implementSwipeToRefresh
import Dependencies.implementVpnManager

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("kotlinx-serialization")
    id("org.jlleitschuh.gradle.ktlint")
    id("de.mannodermaus.android-junit5") version "1.12.0.0"
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.kape.vpnregionselection"
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
        isCoreLibraryDesugaringEnabled = true
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
    coreLibraryDesugaring(desugarJdkLibs)

    implementation(project(":core:utils"))
    implementation(project(":core:router"))
    implementation(project(":core:regions"))
    implementation(project(":core:vpnconnect"))
    implementation(project(":core:localprefs:regions"))
    implementation(project(":core:localprefs:regions:data"))
    implementation(project(":core:localprefs:connection"))
    implementation(project(":core:localprefs:settings"))
    implementation(project(":capabilities:ui"))
    implementation(project(":features:appbar"))

    implementFeatureModule()
    implementVpnManager()
    implementSerialization()
    implementSwipeToRefresh()
}