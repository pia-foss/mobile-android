import Dependencies.desugarJdkLibs
import Dependencies.implementSerialization

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("org.jlleitschuh.gradle.ktlint")
}

android {
    namespace = "com.kape.settings.prefs"
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
    implementation(project(":core:utils"))
    implementation(project(":core:localprefs:settings:data"))
    implementSerialization()
}