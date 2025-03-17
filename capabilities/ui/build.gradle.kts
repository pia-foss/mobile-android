import Dependencies.KOTLIN_COMPILER_EXTENSION
import Dependencies.desugarJdkLibs
import Dependencies.implementCompose
import Dependencies.implementComposeNavigation
import Dependencies.implementComposeTv
import Dependencies.implementKoin
import Dependencies.implementSwipeToRefresh

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("org.jlleitschuh.gradle.ktlint")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.kape.ui"
    compileSdk = 35

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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

    implementation(project(":core:router"))
    implementation(project(":core:utils"))

    implementCompose()
    implementComposeTv()
    implementComposeNavigation()
    implementKoin()
    implementSwipeToRefresh()
}