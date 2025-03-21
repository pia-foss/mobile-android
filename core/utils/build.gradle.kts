import Dependencies.desugarJdkLibs
import Dependencies.implementCoroutines
import Dependencies.implementCrypto
import Dependencies.implementSerialization
import Dependencies.implementVpnManager

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("org.jlleitschuh.gradle.ktlint")
    id("kotlinx-serialization")
}

android {
    namespace = "com.kape.utils"
    compileSdk = 35

    defaultConfig {
        minSdk = 24
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    coreLibraryDesugaring(desugarJdkLibs)
    implementCrypto()
    implementCoroutines()
    implementVpnManager()
    implementSerialization()
}