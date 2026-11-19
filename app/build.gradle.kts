import com.android.build.api.dsl.ApplicationExtension
import org.gradle.kotlin.dsl.configure
import java.net.URI

plugins {
    alias(libs.plugins.pia.android.application)
}

// Keep these as static literals: F-Droid reads them from this file to build the release.
val googleAppVersionCode = 719
val noInAppVersionCode = 10719
val appVersionName = "4.1.6"

configure<ApplicationExtension> {
    namespace = "com.kape.vpn"

    defaultConfig {
        applicationId = "com.kape.vpn"
        versionName = appVersionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        testInstrumentationRunnerArguments +=
            mapOf(
                "clearPackageData" to "true",
                "PIA_VALID_USERNAME" to System.getenv("PIA_VALID_USERNAME").orEmpty(),
                "PIA_VALID_PASSWORD" to System.getenv("PIA_VALID_PASSWORD").orEmpty(),
                "PIA_VALID_DIP_TOKEN" to System.getenv("PIA_VALID_DIP_TOKEN").orEmpty(),
            )
        vectorDrawables.useSupportLibrary = true
    }

    productFlavors {
        getByName(ProviderFlavor.AMAZON) {
            applicationId = "com.privateinternetaccess.android"
            versionCode = noInAppVersionCode
            buildConfigField("String", "UPDATE_URL", "\"amzn://apps/android?p=com.privateinternetaccess.android\"")
        }
        getByName(ProviderFlavor.GOOGLE) {
            applicationId = "com.privateinternetaccess.android"
            versionCode = googleAppVersionCode
            buildConfigField("String", "UPDATE_URL", "\"market://details?id=com.privateinternetaccess.android\"")
        }
        listOf(ProviderFlavor.NO_IN_APP, ProviderFlavor.META, ProviderFlavor.FDROID).forEach { flavor ->
            getByName(flavor) {
                applicationId = "com.privateinternetaccess.android"
                versionCode = noInAppVersionCode
                buildConfigField("String", "UPDATE_URL", "\"\"")
            }
        }
    }

    testOptions {
        execution = "ANDROIDX_TEST_ORCHESTRATOR"
        unitTests.isReturnDefaultValues = true
    }

    buildTypes {
        release {
            optimization {
                enable = true
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
        }
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
    }

    buildFeatures {
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/versions/9/OSGI-INF/MANIFEST.MF"
            excludes += "/META-INF/LICENSE.md"
            excludes += "/META-INF/LICENSE-notice.md"
        }
        // Keeps native libraries extracted on install, as the VPN processes expect.
        jniLibs.useLegacyPackaging = true
    }

    // F-Droid rejects builds that embed this Google-encrypted dependency metadata.
    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }
}

dependencies {
    coreLibraryDesugaring(libs.desugar)
    implementation(libs.bundles.android.bundle)

    implementation(project(":core:network"))
    implementation(project(":core:utils"))
    implementation(project(":core:regions"))
    implementation(project(":core:account"))
    implementation(project(":capabilities:featureflags"))
    implementation(project(":core:localprefs:prefs"))
    implementation(project(":capabilities:ui"))
    implementation(project(":features:splash"))
    implementation(project(":features:login"))
    implementation(project(":features:signup"))
    implementation(project(":capabilities:appbar"))
    implementation(project(":features:dedicatedip"))
    implementation(project(":core:vpnconnect"))
    implementation(project(":core:obfuscator"))
    implementation(project(":core:portforwarding"))
    implementation(project(":capabilities:buildconfig"))
    implementation(project(":capabilities:csi"))
    implementation(project(":capabilities:shareevents"))
    implementation(project(":core:vpnlauncher"))
    implementation(project(":capabilities:snooze"))
    implementation(project(":capabilities:networkmanagement"))
    implementation(project(":capabilities:location"))
    implementation(project(":capabilities:rating"))
    implementation(project(":capabilities:sharedui"))
    implementation(project(":features:permissions"))
    implementation(project(":features:inappbrowser"))
    implementation(project(":features:about"))
    implementation(project(":features:tvwelcome"))
    implementation(project(":features:vpnregionselection"))
    implementation(project(":features:obfuscationregionselection"))
    implementation(project(":features:profile"))
    implementation(project(":features:sidemenu"))
    implementation(project(":features:connection"))
    implementation(project(":features:automation"))
    implementation(project(":features:widget"))
    implementation(project(":features:settings"))
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.koin.androidx.workmanager)

    listOf(ProviderFlavor.GOOGLE, ProviderFlavor.AMAZON, ProviderFlavor.NO_IN_APP).forEach { flavor ->
        add("${flavor}Implementation", libs.androidx.core.google.shortcuts)
    }

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.bundles.junit5.bundle)
    testRuntimeOnly(libs.bundles.junit5.runtime.bundle)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.mockk)
    androidTestImplementation(libs.bundles.androidtest.bundle)
    androidTestUtil(libs.androidx.test.orchestrator)
}

ktlint {
    android.set(true)
    outputColorName.set("RED")
}

tasks.register("updateRegionsInformation") {
    group = "custom"
    description = "Fetches the latest VPN and Shadowsocks region lists into app/src/main/assets"
    doLast {
        val assetsDir = File("$rootDir/app/src/main/assets").apply { mkdirs() }
        mapOf(
            "https://serverlist.piaservers.net/vpninfo/regions/v2" to "metadata-regions.json",
            "https://serverlist.piaservers.net/vpninfo/servers/v7" to "vpn-regions.json",
            "https://serverlist.piaservers.net/shadow_socks" to "shadowsocks-regions.json",
        ).forEach { (url, fileName) ->
            File(assetsDir, fileName).writeText(URI(url).toURL().readText())
        }
        println("Region information files updated. Review the diff and commit.")
    }
}

// Read by the release pipeline.
tasks.register("printVersionName") {
    doLast { println(appVersionName) }
}

tasks.register("printPlayStoreVersionCode") {
    doLast { println(googleAppVersionCode) }
}

tasks.register("printNonPlayStoreVersionCode") {
    doLast { println(noInAppVersionCode) }
}