import Dependencies.desugarJdkLibs
import Dependencies.implementAccount
import Dependencies.implementAndroidBase
import Dependencies.implementCompose
import Dependencies.implementComposeNavigation
import Dependencies.implementCoroutines
import Dependencies.implementKoin
import Dependencies.implementKpi
import Dependencies.implementMultiplatformSettings
import Dependencies.implementRegions
import Dependencies.implementViewModel
import Dependencies.implementVpnManager

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.kape.vpn"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.kape.vpn"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    flavorDimensions.add("provider")
    productFlavors {
        create("amazon") {
            dimension = "provider"
            applicationId = "com.privateinternetaccess.android"
        }
        create("google") {
            dimension = "provider"
            applicationId = "com.privateinternetaccess.android"
        }
    }

    sourceSets {
        getByName("amazon") {
            manifest.srcFile("amazon/AndroidManifest.xml")
        }
        getByName("google") {
            manifest.srcFile("google/AndroidManifest.xml")
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
    buildFeatures {
        buildConfig = true
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    coreLibraryDesugaring(desugarJdkLibs)

    implementation(project(":core:router"))
    implementation(project(":core:utils"))
    implementation(project(":core:localprefs:settings"))
    implementation(project(":core:localprefs:connection"))
    implementAccount()
    implementation(project(":core:payments"))
    implementation(project(":core:vpnconnect"))
    implementation(project(":core:portforwarding"))
    implementKpi()
    implementRegions()

    implementation(project(":capabilities:ui"))
    implementation(project(":capabilities:shareevents"))
    implementation(project(":capabilities:notifications"))

    implementation(project(":features:splash"))
    implementation(project(":features:signup"))
    implementation(project(":features:login"))
    implementation(project(":features:settings"))
    implementation(project(":features:vpnpermission"))
    implementation(project(":features:profile"))
    implementation(project(":features:regionselection"))
    implementation(project(":features:sidemenu"))
    implementation(project(":features:connection"))
    implementation(project(":features:appbar"))
    implementation(project(":features:dedicatedip"))

    implementAndroidBase()
    implementViewModel()
    implementCompose()
    implementComposeNavigation()
    implementKoin()
    implementMultiplatformSettings()
    implementVpnManager()
    implementCoroutines()
}
