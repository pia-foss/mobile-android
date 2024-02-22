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
    id("de.mannodermaus.android-junit5") version "1.10.0.0"
    id("org.jetbrains.kotlinx.kover")
}

android {
    namespace = "com.kape.vpnconnect"
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(project(":core:utils"))
    implementation(project(":core:portforwarding"))
    implementation(project(":core:obfuscator"))
    implementation(project(":core:localprefs:regions"))
    implementation(project(":core:localprefs:settings"))
    implementation(project(":core:localprefs:settings:data"))
    implementation(project(":core:localprefs:connection"))
    implementation(project(":capabilities:shareevents"))
    implementation(project(":capabilities:ui"))
    implementVpnManager()
    implementKoin()
    implementAccount()
    implementSerialization()
    implementCoroutines()

    implementTest()
}