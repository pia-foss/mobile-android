import Dependencies.implementFeatureModule

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("org.jlleitschuh.gradle.ktlint")
}

android {
    namespace = "com.kape.automation"
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
        kotlinCompilerExtensionVersion = "1.5.1"
    }
}

dependencies {

    implementation(project(":core:router"))
    implementation(project(":core:utils"))
    implementation(project(":core:localprefs:settings"))
    implementation(project(":capabilities:ui"))
    implementation(project(":capabilities:location"))
    implementation(project(":features:appbar"))

    implementFeatureModule()
}