import Dependencies.KOTLIN_COMPILER_EXTENSION
import Dependencies.desugarJdkLibs
import Dependencies.implementGlance
import Dependencies.implementKoin
import Dependencies.implementVpnManager

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("org.jlleitschuh.gradle.ktlint")
    id("de.mannodermaus.android-junit5") version "1.12.0.0"
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.kape.widget"
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
    testOptions {
        unitTests {
            isReturnDefaultValues = true
        }
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
    implementGlance()
    implementKoin()
    implementVpnManager()
    implementation(project(":core:vpnconnect"))
    implementation(project(":core:vpnlauncher"))
    implementation(project(":capabilities:ui"))
}