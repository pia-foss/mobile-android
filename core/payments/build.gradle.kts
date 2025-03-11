import Dependencies.KOTLIN_COMPILER_EXTENSION
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
    id("org.jetbrains.kotlinx.kover")
    id("de.mannodermaus.android-junit5") version "1.10.0.0"
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.kape.payments"
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
        kotlinCompilerExtensionVersion = KOTLIN_COMPILER_EXTENSION
    }
}

dependencies {
    coreLibraryDesugaring(desugarJdkLibs)

    implementAccount()
    implementation(project(":core:utils"))
    implementation(project(":core:localprefs:payments"))
    implementation(project(":core:localprefs:payments:data"))

    implementCrypto()
    implementKoin()
    implementCoroutines()
    implementSerialization()
    implementPayments()
    implementTest()
}