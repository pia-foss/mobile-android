import Dependencies.KOTLIN_COMPILER_EXTENSION
import Dependencies.desugarJdkLibs
import Dependencies.implementAccount
import Dependencies.implementAndroidBase
import Dependencies.implementAndroidUiTest
import Dependencies.implementCompose
import Dependencies.implementComposeNavigation
import Dependencies.implementCoroutines
import Dependencies.implementCsi
import Dependencies.implementKoin
import Dependencies.implementKpi
import Dependencies.implementMultiplatformSettings
import Dependencies.implementObfuscator
import Dependencies.implementPayments
import Dependencies.implementRegions
import Dependencies.implementViewModel
import Dependencies.implementVpnManager
import java.net.URL

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kape.licenses")
    id("org.jetbrains.kotlinx.kover")
    id("org.jetbrains.kotlin.plugin.compose")
}

val googleAppVersionCode = 683
val amazonAppVersionCode = googleAppVersionCode.plus(10000)
val noInAppVersionCode = googleAppVersionCode.plus(10000)
val appVersionName = "4.0.19"

android {
    namespace = "com.kape.vpn"
    compileSdk = 35

    defaultConfig {
        testInstrumentationRunnerArguments += mapOf("clearPackageData" to "true")
        applicationId = "com.kape.vpn"
        minSdk = 24
        targetSdk = 35
        versionName = appVersionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }
    testOptions {
        execution = "ANDROIDX_TEST_ORCHESTRATOR"
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
            versionCode = amazonAppVersionCode

            buildConfigField(
                "String",
                "UPDATE_URL",
                "\"amzn://apps/android?com.privateinternetaccess.android\"",
            )
        }
        create("google") {
            dimension = "provider"
            applicationId = "com.privateinternetaccess.android"
            versionCode = googleAppVersionCode

            buildConfigField(
                "String",
                "UPDATE_URL",
                "\"market://details?id=com.privateinternetaccess.android\"",
            )
        }
        create("noinapp") {
            dimension = "provider"
            applicationId = "com.privateinternetaccess.android"
            versionCode = noInAppVersionCode

            buildConfigField(
                "String",
                "UPDATE_URL",
                "\"\"",
            )
        }
    }

    sourceSets {
        getByName("amazon") {
            manifest.srcFile("amazon/AndroidManifest.xml")
        }
        getByName("google") {
            manifest.srcFile("google/AndroidManifest.xml")
        }
        getByName("noinapp") {
            manifest.srcFile("noinapp/AndroidManifest.xml")
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
        kotlinCompilerExtensionVersion = KOTLIN_COMPILER_EXTENSION
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/versions/9/OSGI-INF/MANIFEST.MF"
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
    implementation(project(":core:httpclient"))
    implementation(project(":core:localprefs:settings"))
    implementation(project(":core:localprefs:settings:data"))
    implementation(project(":core:localprefs:connection"))
    implementation(project(":core:localprefs:csi"))
    implementation(project(":core:localprefs:networkmanagement"))
    implementation(project(":core:localprefs:networkmanagement:data"))
    implementation(project(":core:localprefs:customization"))
    implementation(project(":core:localprefs:customization:data"))
    implementation(project(":core:localprefs:rating:data"))
    implementation(project(":core:localprefs:rating"))
    implementation(project(":core:localprefs:shortcut"))
    implementAccount()
    implementation(project(":core:payments"))
    implementation(project(":core:vpnconnect"))
    implementation(project(":core:vpnlauncher"))
    implementation(project(":core:portforwarding"))
    implementation(project(":core:regions"))
    implementation(project(":core:obfuscator"))
    implementKpi()
    implementRegions()
    implementCsi()
    implementObfuscator()

    implementation(project(":capabilities:ui"))
    implementation(project(":capabilities:shareevents"))
    implementation(project(":capabilities:notifications"))
    implementation(project(":capabilities:csi"))
    implementation(project(":capabilities:networkmanagement"))
    implementation(project(":capabilities:snooze"))
    implementation(project(":capabilities:buildconfig"))
    implementation(project(":capabilities:featureflags"))

    implementation(project(":features:splash"))
    implementation(project(":features:tvwelcome"))
    implementation(project(":features:signup"))
    implementation(project(":features:login"))
    implementation(project(":features:settings"))
    implementation(project(":features:permissions"))
    implementation(project(":features:profile"))
    implementation(project(":features:vpnregionselection"))
    implementation(project(":features:obfuscationregionselection"))
    implementation(project(":features:sidemenu"))
    implementation(project(":features:connection"))
    implementation(project(":features:appbar"))
    implementation(project(":features:dedicatedip"))
    implementation(project(":features:automation"))
    implementation(project(":features:widget"))
    implementation(project(":features:about"))
    implementation(project(":features:customization"))
    implementation(project(":features:inappbrowser"))
    implementation(project(":features:rating"))

    implementAndroidBase()
    implementViewModel()
    implementCompose()
    implementComposeNavigation()
    implementKoin()
    implementMultiplatformSettings()
    implementVpnManager()
    implementCoroutines()

    implementPayments()

    implementAndroidUiTest()

    // Test coverage
    kover(project(":capabilities:csi"))
    kover(project(":capabilities:shareevents"))
    kover(project(":core:payments"))
    kover(project(":core:regions"))
    kover(project(":core:vpnconnect"))
    kover(project(":features:connection"))
    kover(project(":features:dedicatedip"))
    kover(project(":features:login"))
    kover(project(":features:profile"))
    kover(project(":features:signup"))
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

koverReport {
    filters {
        excludes {
            // exclusion rules - classes to exclude from report
            classes("com.kape.*.di.*", "com.kape.*.ui.*")
            annotatedBy("*.KoverIgnore")
        }
        includes {
            // inclusion rules - classes only those that will be present in reports
            classes("com.kape.*.data.*", "com.kape.*.domain.*")
        }
    }
}