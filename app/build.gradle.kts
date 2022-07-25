plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    compileSdk = 32

    defaultConfig {
        applicationId = "com.kape.pia"
        minSdk = 23
        targetSdk = 32
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
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = Compose.COMPOSE_VERSION
    }
    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    namespace = "com.kape.vpn"
}

dependencies {
    implementation(project(mapOf("path" to ":connection")))
    implementation(project(mapOf("path" to ":login")))
    implementation(project(mapOf("path" to ":account")))
    implementation(project(mapOf("path" to ":profile")))
    implementation(project(mapOf("path" to ":region_selection")))
    implementation(project(mapOf("path" to ":vpn_permissions")))
    implementation(project(mapOf("path" to ":sidemenu")))
    implementation(project(mapOf("path" to ":regions")))
    implementation(project(mapOf("path" to ":uicomponents")))
    implementation(project(mapOf("path" to ":router")))
    implementation(project(mapOf("path" to ":splash")))
    implementation(project(mapOf("path" to ":payments")))

    coreLibraryDesugaring(Android.desugarJdkLibs)

    implementation(Android.androidCore)
    implementation(Compose.ui)
    implementation(Compose.material)
    implementation(Compose.preview)
    implementation(Android.lifecycle)
    implementation(Compose.activity)
    implementation(Compose.navigation)

    implementation(Koin.core)
    implementation(Koin.android)
    implementation(Koin.compose)

    implementation(Android.multiplatformSettings)
    implementation(Android.crypto)

    "amazonImplementation"(Payments.amazon)
    "googleImplementation"(Payments.google)

    testImplementation(UnitTest.junit)
    androidTestImplementation(AndroidTest.jUnit)
    androidTestImplementation(AndroidTest.espressoCore)
    androidTestImplementation(AndroidTest.composeUI)
    debugImplementation(DebugTest.composeUI)
}