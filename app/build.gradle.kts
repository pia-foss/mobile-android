import java.net.URL

plugins {
    alias(libs.plugins.application)
    alias(libs.plugins.kotlin)
    alias(libs.plugins.compose)
    alias(libs.plugins.configuration)
}

val googleAppVersionCode = 685
val amazonAppVersionCode = googleAppVersionCode.plus(10000)
val noInAppVersionCode = googleAppVersionCode.plus(10000)
val appVersionName = "4.0.21"

android {
    namespace = "com.kape.vpn"
    defaultConfig {
        testInstrumentationRunnerArguments += mapOf("clearPackageData" to "true")
        applicationId = "com.kape.vpn"
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
    }
    buildFeatures {
        buildConfig = true
        compose = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/versions/9/OSGI-INF/MANIFEST.MF"
            excludes += "/META-INF/LICENSE.md"
            excludes += "/META-INF/LICENSE-notice.md"
        }
        jniLibs {
            useLegacyPackaging = true
        }
    }
}

dependencies {
    coreLibraryDesugaring(libs.desugar)
    val composeBom = platform(libs.compose.bom)
    implementation(composeBom)
    androidTestImplementation(composeBom)

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
    implementation(project(":core:payments"))
    implementation(project(":core:vpnconnect"))
    implementation(project(":core:vpnlauncher"))
    implementation(project(":core:portforwarding"))
    implementation(project(":core:regions"))
    implementation(project(":core:obfuscator"))
    implementation(libs.kape.account)
    implementation(libs.kape.kpi)
    implementation(libs.kape.regions)
    implementation(libs.kape.csi)
    implementation(libs.kape.obfuscator)
    implementation(libs.kape.vpnmanager)

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

    implementation(libs.bundles.android)
    implementation(libs.bundles.compose)
    androidTestImplementation(libs.compose.android.test)
    debugImplementation(libs.bundles.debugtest)
    implementation(libs.bundles.koin)
    testImplementation(libs.bundles.kointest)
    androidTestImplementation(libs.bundles.koinandroidtest)
    implementation(libs.multiplatform.settings)
    implementation(libs.coroutines)
    testImplementation(libs.coroutines.test)
    "googleImplementation"(libs.billing.google)
    "amazonImplementation"(libs.billing.amazon)
    androidTestImplementation(libs.bundles.androidtest)
    "androidTestUtil"(libs.orchestrator)
}

task("fetchRegionsInformation") {
    File("$rootDir/app/src/main/assets/metadata-regions.json")
        .writeText(
            URL("https://serverlist.piaservers.net/vpninfo/regions/v2").readText(),
        )
    File("$rootDir/app/src/main/assets/vpn-regions.json")
        .writeText(
            URL("https://serverlist.piaservers.net/vpninfo/servers/v6").readText(),
        )
    File("$rootDir/app/src/main/assets/shadowsocks-regions.json")
        .writeText(
            URL("https://serverlist.piaservers.net/shadow_socks").readText(),
        )
}