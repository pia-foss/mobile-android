import Dependencies.implementAccount
import Dependencies.implementCoroutines
import Dependencies.implementKoin
import Dependencies.implementSerialization
import Dependencies.implementTest
import Dependencies.implementVpnManager

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("org.jlleitschuh.gradle.ktlint")
    id("de.mannodermaus.android-junit5") version "1.8.2.0"
}

android {
    namespace = "com.kape.vpnconnect"
    compileSdk = 34

    defaultConfig {
        minSdk = 21
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
    implementVpnManager()
    implementKoin()
    implementAccount()
    implementSerialization()
    implementCoroutines()

    implementTest()
}