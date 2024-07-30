import Dependencies.implementCoroutines
import Dependencies.implementKtor
import Dependencies.implementSerialization
import Dependencies.implementSpongyCastle

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("kotlinx-serialization")
    id("org.jlleitschuh.gradle.ktlint")
}

android {
    namespace = "com.kape.httpclient"
    compileSdk = 34

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
    implementCoroutines()
    implementKtor()
    implementSpongyCastle()
    implementSerialization()
}