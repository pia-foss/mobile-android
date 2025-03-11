import Dependencies.implementCoroutines
import Dependencies.implementKoin
import Dependencies.implementObfuscator
import Dependencies.implementTest

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("org.jlleitschuh.gradle.ktlint")
    id("de.mannodermaus.android-junit5") version "1.10.0.0"
}

android {
    namespace = "com.kape.obfuscator"
    compileSdk = 35

    defaultConfig {
        minSdk = 24
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
    implementObfuscator()
    implementCoroutines()
    implementKoin()
    implementTest()
}