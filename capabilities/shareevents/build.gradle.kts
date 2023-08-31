import Dependencies.desugarJdkLibs
import Dependencies.implementFeatureModule
import Dependencies.implementKpi
import Dependencies.implementSerialization

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("org.jlleitschuh.gradle.ktlint")
    id("de.mannodermaus.android-junit5") version "1.8.2.0"
}

android {
    namespace = "com.kape.shareevents"
    compileSdk = 34

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

    implementKpi()
    implementation(project(":core:utils"))
    implementation(project(":core:localprefs:shareevents"))

    implementSerialization()
    implementFeatureModule()
}