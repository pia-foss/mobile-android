import Dependencies.KOTLIN_COMPILER_EXTENSION

plugins {
    alias(libs.plugins.library)
    alias(libs.plugins.kotlin)
    alias(libs.plugins.serialization)
    alias(libs.plugins.ktlint)

    alias(libs.plugins.junit5)
    alias(libs.plugins.compose)
}

android {
    namespace = "com.kape.payments"
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
    testOptions {
        unitTests {
            isReturnDefaultValues = true
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
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = KOTLIN_COMPILER_EXTENSION
    }
}

dependencies {
    coreLibraryDesugaring(libs.desugar)

    implementation(libs.kape.account)
    implementation(project(":core:utils"))
    implementation(project(":core:localprefs:payments"))
    implementation(project(":core:localprefs:payments:data"))

    implementation(libs.crypto)
    implementation(libs.bundles.koin)
    testImplementation(libs.bundles.kointest)
    androidTestImplementation(libs.bundles.koinandroidtest)
    implementation(libs.coroutines)
    testImplementation(libs.coroutines.test)
    androidTestImplementation(libs.bundles.coroutines.androidtest)
    implementation(libs.bundles.serialization)
    "googleImplementation"(libs.billing.google)
    "amazonImplementation"(libs.billing.amazon)
    testImplementation(libs.bundles.test)
    runtimeOnly(libs.launcher)
}