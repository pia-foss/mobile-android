import Dependencies.desugarJdkLibs
import Dependencies.implementAccount
import Dependencies.implementCoroutines
import Dependencies.implementCrypto
import Dependencies.implementKoin
import Dependencies.implementPayments
import Dependencies.implementSerialization
import Dependencies.implementTest

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("kotlinx-serialization")
    id("org.jlleitschuh.gradle.ktlint")
    id("de.mannodermaus.android-junit5") version "1.8.2.0"
}

android {
    namespace = "com.kape.payments"
    compileSdk = 33

    defaultConfig {
        minSdk = 21
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
    testOptions {
        unitTests {
            isReturnDefaultValues = true
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
    implementation(project(":core:utils"))

    implementCrypto()
    implementKoin()
    implementCoroutines()
    implementSerialization()
    implementPayments()
    implementTest()
}