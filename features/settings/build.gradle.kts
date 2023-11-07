import Dependencies.desugarJdkLibs
import Dependencies.implementColorPicker
import Dependencies.implementDrawablePainter
import Dependencies.implementFeatureModule
import Dependencies.implementSerialization

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("org.jlleitschuh.gradle.ktlint")
    id("kotlinx-serialization")
    id("de.mannodermaus.android-junit5") version "1.8.2.0"
}

android {
    namespace = "com.kape.settings"
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

    implementation(project(":core:router"))
    implementation(project(":core:utils"))
    implementation(project(":core:regions"))
    implementation(project(":core:vpnconnect"))
    implementation(project(":core:localprefs:settings"))
    implementation(project(":core:localprefs:settings:data"))
    implementation(project(":capabilities:ui"))
    implementation(project(":capabilities:shareevents"))
    implementation(project(":capabilities:csi"))
    implementation(project(":capabilities:location"))
    implementation(project(":features:appbar"))
    implementation(project(":features:regionselection"))

    implementFeatureModule()
    implementSerialization()
    implementDrawablePainter()
    implementColorPicker()
}