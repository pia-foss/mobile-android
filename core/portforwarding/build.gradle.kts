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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(project(":core:utils"))
    implementation(project(":core:httpclient"))
    implementation(project(":core:localprefs:connection"))
    implementation(project(":core:localprefs:settings"))
    implementation(project(":core:localprefs:settings:data"))
    implementCoroutines()
    implementSerialization()
    implementKtor()
    implementKoin()
    implementSpongyCastle()
}