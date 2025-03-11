import Dependencies.implementAndroidBase
import Dependencies.implementKoin

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("org.jlleitschuh.gradle.ktlint")
    id("de.mannodermaus.android-junit5") version "1.10.0.0"
}

android {
    namespace = "com.kape.snooze"
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
}

dependencies {
    implementKoin()
    implementAndroidBase()

    implementation(project(":core:utils"))
    implementation(project(":core:localprefs:connection"))
    implementation(project(":core:vpnlauncher"))
}