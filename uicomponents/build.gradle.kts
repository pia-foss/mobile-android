plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    compileSdk = 33

    defaultConfig {
        minSdk = 23
        targetSdk = 33

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    namespace = "com.kape.uicomponents"
}

dependencies {
    implementation(project(mapOf("path" to ":router")))
    coreLibraryDesugaring(Android.desugarJdkLibs)

    implementation(Android.androidCore)
    implementation(Android.swipeRefreshLayout)

    implementation(Compose.ui)
    implementation(Compose.material)
    implementation(Compose.activity)
    implementation(Compose.preview)

    implementation(Koin.core)
    implementation(Koin.android)
    implementation(Koin.compose)

    debugImplementation(DebugTest.composeUI)
    debugImplementation(DebugTest.composeManifest)
}