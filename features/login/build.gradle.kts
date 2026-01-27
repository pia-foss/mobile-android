plugins {
    alias(libs.plugins.library)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.junit5)
    alias(libs.plugins.configuration)
    alias(libs.plugins.compose)
}

android {
    namespace = "com.kape.login"
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

    implementation(libs.kape.account)

    implementation(project(":core:payments"))
    implementation(project(":core:router"))
    implementation(project(":core:utils"))
    implementation(project(":core:vpnconnect"))

    implementation(project(":capabilities:buildconfig"))
    implementation(project(":capabilities:ui"))

    // prefs
    implementation(project(":core:localprefs:connection"))
    implementation(project(":core:localprefs:csi"))
    implementation(project(":core:localprefs:customization"))
    implementation(project(":core:localprefs:dip"))
    implementation(project(":core:localprefs:networkmanagement"))
    implementation(project(":core:localprefs:payments"))
    implementation(project(":core:localprefs:regions"))
    implementation(project(":core:localprefs:settings"))
    implementation(project(":core:localprefs:shareevents"))
    implementation(project(":core:localprefs:signup"))
    implementation(project(":core:localprefs:rating"))
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
}