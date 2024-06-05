import Dependencies.KOTLIN_COMPILER_EXTENSION
import Dependencies.desugarJdkLibs
import Dependencies.implementAccount
import Dependencies.implementFeatureModule
import Dependencies.implementSerialization
import Dependencies.implementVpnManager

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("org.jlleitschuh.gradle.ktlint")
    id("de.mannodermaus.android-junit5") version "1.10.0.0"
    id("org.jetbrains.kotlinx.kover")
}

android {
    namespace = "com.kape.connection"
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

    implementation(project(":capabilities:ui"))
    implementation(project(":capabilities:notifications"))
    implementation(project(":capabilities:shareevents"))
    implementation(project(":capabilities:snooze"))
    implementation(project(":core:router"))
    implementation(project(":core:payments"))
    implementation(project(":core:regions"))
    implementation(project(":core:vpnconnect"))
    implementation(project(":core:portforwarding"))
    implementation(project(":core:localprefs:connection"))
    implementation(project(":core:localprefs:settings"))
    implementation(project(":core:localprefs:settings:data"))
    implementation(project(":core:localprefs:customization"))
    implementation(project(":core:localprefs:customization:data"))
    implementation(project(":core:localprefs:dip"))
    implementation(project(":core:localprefs:csi"))
    implementation(project(":core:localprefs:regions"))
    implementation(project(":core:localprefs:regions:data"))
    implementation(project(":core:utils"))
    implementation(project(":features:appbar"))
    implementation(project(":features:settings"))
    implementation(project(":features:sidemenu"))
    implementation(project(":features:vpnregionselection"))
    implementation(project(":features:dedicatedip"))
    implementation(project(":features:rating"))

    implementFeatureModule()
    implementVpnManager()
    implementAccount()
    implementSerialization()
}