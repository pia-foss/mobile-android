import Dependencies.KOTLIN_COMPILER_EXTENSION

plugins {
    alias(libs.plugins.library)
    alias(libs.plugins.kotlin)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.junit5)

    alias(libs.plugins.compose)
}

android {
    namespace = "com.kape.login"
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