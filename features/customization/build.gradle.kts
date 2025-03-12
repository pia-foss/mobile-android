import Dependencies.KOTLIN_COMPILER_EXTENSION
import Dependencies.desugarJdkLibs
import Dependencies.implementFeatureModule
import Dependencies.implementReorderable

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("org.jlleitschuh.gradle.ktlint")
    id("de.mannodermaus.android-junit5") version "1.12.0.0"
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.kape.customization"
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

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    testOptions {
        unitTests {
            isReturnDefaultValues = true
        }
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
    implementReorderable()
    implementFeatureModule()
    implementation(project(":core:router"))
    implementation(project(":core:utils"))
    implementation(project(":core:localprefs:customization"))
    implementation(project(":core:localprefs:customization:data"))
    implementation(project(":core:portforwarding"))
    implementation(project(":core:localprefs:settings"))
    implementation(project(":core:localprefs:settings:data"))
    implementation(project(":capabilities:ui"))
    implementation(project(":features:appbar"))
    implementation(project(":features:connection"))
}