import Dependencies.desugarJdkLibs
import Dependencies.implementAccount
import Dependencies.implementAndroidBase
import Dependencies.implementAndroidUiTest
import Dependencies.implementCompose
import Dependencies.implementComposeNavigation
import Dependencies.implementCoroutines
import Dependencies.implementCsi
import Dependencies.implementGlance
import Dependencies.implementKoin
import Dependencies.implementKpi
import Dependencies.implementMultiplatformSettings
import Dependencies.implementRegions
import Dependencies.implementViewModel
import Dependencies.implementVpnManager
import java.net.URL

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kape.licenses")
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
        debug {
            buildConfigField(
                "String",
                "PIA_VALID_USERNAME",
                "\"${System.getenv("PIA_VALID_USERNAME")}\"",
            )
            buildConfigField(
                "String",
                "PIA_VALID_PASSWORD",
                "\"${System.getenv("PIA_VALID_PASSWORD")}\"",
            )
            buildConfigField(
                "String",
                "PIA_VALID_DIP_TOKEN",
                "\"${System.getenv("PIA_VALID_DIP_TOKEN")}\"",
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
        jniLibs {
            useLegacyPackaging = true
        }
    }
}

dependencies {
    coreLibraryDesugaring(desugarJdkLibs)

    implementation(project(":core:router"))
    implementation(project(":core:utils"))
    implementation(project(":core:localprefs:settings"))
    implementation(project(":core:localprefs:settings:data"))
    implementation(project(":core:localprefs:connection"))
    implementation(project(":core:localprefs:csi"))
    implementation(project(":core:localprefs:networkmanagement"))
    implementation(project(":core:localprefs:networkmanagement:data"))
    implementAccount()
    implementation(project(":core:payments"))
    implementation(project(":core:vpnconnect"))
    implementation(project(":core:vpnlauncher"))
    implementation(project(":core:portforwarding"))
    implementation(project(":core:regions"))
    implementKpi()
    implementRegions()
    implementCsi()

    implementation(project(":capabilities:ui"))
    implementation(project(":capabilities:shareevents"))
    implementation(project(":capabilities:notifications"))
    implementation(project(":capabilities:csi"))
    implementation(project(":capabilities:networkmanagement"))

    implementation(project(":features:splash"))
    implementation(project(":features:signup"))
    implementation(project(":features:login"))
    implementation(project(":features:settings"))
    implementation(project(":features:permissions"))
    implementation(project(":features:profile"))
    implementation(project(":features:vpnregionselection"))
    implementation(project(":features:sidemenu"))
    implementation(project(":features:connection"))
    implementation(project(":features:appbar"))
    implementation(project(":features:dedicatedip"))
    implementation(project(":features:automation"))
    implementation(project(":features:widget"))

    implementAndroidBase()
    implementViewModel()
    implementCompose()
    implementComposeNavigation()
    implementKoin()
    implementMultiplatformSettings()
    implementVpnManager()
    implementCoroutines()

    implementAndroidUiTest()
}

task("fetchRegionsInformation"){
    File("$rootDir/app/src/main/assets/metadata-regions.json")
        .writeText(
            URL("https://serverlist.piaservers.net/vpninfo/regions/v2").readText()
        )
    File("$rootDir/app/src/main/assets/vpn-regions.json")
        .writeText(
            URL("https://serverlist.piaservers.net/vpninfo/servers/v6").readText()
        )
    File("$rootDir/app/src/main/assets/shadowsocks-regions.json")
        .writeText(
            URL("https://serverlist.piaservers.net/shadow_socks").readText()
        )
}