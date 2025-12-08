plugins {
    alias(libs.plugins.library)
    alias(libs.plugins.configuration)
    alias(libs.plugins.kotlin)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.junit5)
    alias(libs.plugins.compose)
}

android {
    namespace = "com.kape.connection"
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
    }
    testOptions {
        unitTests {
            isReturnDefaultValues = true
        }
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    coreLibraryDesugaring(libs.desugar)

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
    implementation(project(":core:localprefs:shortcut"))
    implementation(project(":core:utils"))
    implementation(project(":features:appbar"))
    implementation(project(":features:settings"))
    implementation(project(":features:sidemenu"))
    implementation(project(":features:vpnregionselection"))
    implementation(project(":features:dedicatedip"))
    implementation(project(":features:rating"))

    implementation(libs.bundles.compose)
    androidTestImplementation(libs.bundles.composeandroidtest)
    implementation(libs.bundles.android)
    implementation(libs.bundles.koin)
    testImplementation(libs.bundles.kointest)
    androidTestImplementation(libs.bundles.koinandroidtest)
    implementation(libs.coroutines)
    testImplementation(libs.coroutines.test)
    androidTestImplementation(libs.bundles.coroutines.androidtest)
    testImplementation(libs.bundles.test)
    runtimeOnly(libs.launcher)
    implementation(libs.kape.vpnmanager)
    implementation(libs.kape.account)
    implementation(libs.bundles.serialization)
    implementation(libs.constraintlayout)
}