import Dependencies.desugarJdkLibs
import Dependencies.implementFeatureModule
import Dependencies.implementKpi
import Dependencies.implementSerialization

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("org.jlleitschuh.gradle.ktlint")
    id("de.mannodermaus.android-junit5") version "1.10.0.0"
    id("org.jetbrains.kotlinx.kover")
}

android {
    namespace = "com.kape.shareevents"
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
        create("noinapp") {
            dimension = "provider"
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
}

dependencies {
    coreLibraryDesugaring(desugarJdkLibs)

    implementKpi()
    implementation(project(":core:utils"))
    implementation(project(":core:localprefs:shareevents"))
    implementation(project(":core:localprefs:settings"))
    implementation(project(":core:localprefs:settings:data"))

    implementSerialization()
    implementFeatureModule()
}