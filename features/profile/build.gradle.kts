import Dependencies.desugarJdkLibs
import Dependencies.implementAccount
import Dependencies.implementFeatureModule

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("org.jlleitschuh.gradle.ktlint")
    id("de.mannodermaus.android-junit5") version "1.8.2.0"
}

android {
    namespace = "com.kape.profile"
    compileSdk = 34

    defaultConfig {
        minSdk = 21
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
        kotlinCompilerExtensionVersion = "1.5.1"
    }
}

dependencies {
    coreLibraryDesugaring(desugarJdkLibs)

    implementAccount()
    implementation(project(":core:router"))
    implementation(project(":core:utils"))
    implementation(project(":capabilities:ui"))

    implementFeatureModule()
}