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

    implementation(project(path = ":login"))
    implementation(project(path = ":account"))
    implementation(project(path = ":profile"))
    implementation(project(path = ":region_selection"))
    implementation(project(path = ":vpn_permissions"))
    implementation(project(path = ":sidemenu"))
    implementation(project(path = ":uicomponents"))
    implementation(project(path = ":regions"))
    implementation(project(path = ":router"))
    implementation(project(path = ":splash"))

    testImplementation(UnitTest.junit)
    androidTestImplementation(AndroidTest.jUnit)
    androidTestImplementation(AndroidTest.espressoCore)
    androidTestImplementation(AndroidTest.composeUI)
    debugImplementation(DebugTest.composeUI)
}