import Dependencies.KOTLIN_COMPILER_EXTENSION
import Dependencies.desugarJdkLibs
import Dependencies.implementAccount
import Dependencies.implementFeatureModule

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("org.jlleitschuh.gradle.ktlint")
    id("de.mannodermaus.android-junit5") version "1.10.0.0"
    id("org.jetbrains.kotlinx.kover")
}

android {
    namespace = "com.kape.login"
    compileSdk = 34

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

    implementAccount()

    implementation(project(":core:payments"))
    implementation(project(":core:router"))
    implementation(project(":core:utils"))
    implementation(project(":core:vpnconnect"))

    implementation(project(":capabilities:buildconfig"))
    implementation(project(":capabilities:ui"))

    // prefs
    implementation(project(":core:localprefs:connection"))
    implementation(project(":core:localprefs:csi"))
    implementation(project(":core:localprefs:customization"))
    implementation(project(":core:localprefs:dip"))
    implementation(project(":core:localprefs:networkmanagement"))
    implementation(project(":core:localprefs:payments"))
    implementation(project(":core:localprefs:regions"))
    implementation(project(":core:localprefs:settings"))
    implementation(project(":core:localprefs:shareevents"))
    implementation(project(":core:localprefs:signup"))
    implementation(project(":core:localprefs:rating"))

    implementFeatureModule()
}