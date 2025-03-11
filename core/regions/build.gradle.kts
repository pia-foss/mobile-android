import Dependencies.implementAccount
import Dependencies.implementCoroutines
import Dependencies.implementKoin
import Dependencies.implementRegions
import Dependencies.implementSerialization
import Dependencies.implementTest

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("kotlinx-serialization")
    id("org.jlleitschuh.gradle.ktlint")
    id("de.mannodermaus.android-junit5") version "1.10.0.0"
    id("org.jetbrains.kotlinx.kover")
}

android {
    namespace = "com.kape.vpnregions"
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
    implementation(project(":core:utils"))
    implementation(project(":core:localprefs:regions"))
    implementation(project(":core:localprefs:dip"))

    implementSerialization()
    implementRegions()
    implementAccount()

    implementCoroutines()
    implementKoin()
    implementTest()
}