import Dependencies.implementCoroutines
import Dependencies.implementKoin
import Dependencies.implementKtor
import Dependencies.implementSerialization
import Dependencies.implementSpongyCastle

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("org.jlleitschuh.gradle.ktlint")
    id("kotlinx-serialization")
}

android {
    namespace = "com.kape.portforwarding"
    compileSdk = 34

    defaultConfig {
        minSdk = 24
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(project(":core:vpnconnect"))
    implementation(project(":core:utils"))
    implementation(project(":core:httpclient"))
    implementation(project(":core:localprefs:connection"))
    implementCoroutines()
    implementSerialization()
    implementKtor()
    implementKoin()
    implementSpongyCastle()
}