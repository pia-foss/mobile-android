import Dependencies.KOTLIN_COMPILER_EXTENSION
import Dependencies.implementFeatureModule

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("org.jlleitschuh.gradle.ktlint")
}

android {
    namespace = "com.kape.tvwelcome"
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
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = KOTLIN_COMPILER_EXTENSION
    }
}

dependencies {

    implementation(project(":core:router"))
    implementation(project(":core:payments"))
    implementation(project(":capabilities:ui"))

    implementFeatureModule()
}