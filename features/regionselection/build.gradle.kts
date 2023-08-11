import Dependencies.desugarJdkLibs
import Dependencies.implementFeatureModule
import Dependencies.implementSerialization
import Dependencies.implementSwipeToRefresh

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("kotlinx-serialization")
}

android {
    namespace = "com.kape.regionselection"
    compileSdk = 33

    defaultConfig {
        minSdk = 21
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

    implementation(project(":core:utils"))
    implementation(project(":core:regions"))
    implementation(project(":core:router"))
    implementation(project(":capabilities:ui"))

    implementFeatureModule()
    implementSerialization()
    implementSwipeToRefresh()
}