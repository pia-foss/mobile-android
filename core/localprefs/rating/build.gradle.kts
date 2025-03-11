import Dependencies.DESUGAR_JDK_LIBS
import Dependencies.implementSerialization

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("org.jlleitschuh.gradle.ktlint")
}

android {
    namespace = "com.kape.rating.prefs"
    compileSdk = 35

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
    coreLibraryDesugaring(DESUGAR_JDK_LIBS)
    implementation(project(":core:utils"))
    implementation(project(":core:localprefs:rating:data"))
    implementSerialization()
}