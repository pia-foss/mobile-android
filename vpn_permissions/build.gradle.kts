plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("de.mannodermaus.android-junit5") version "1.8.2.0"
}

android {
    compileSdk = 33

    defaultConfig {
        minSdk = 23
        targetSdk = 33

        testInstrumentationRunner = "com.kape.vpn_permissions.AndroidTestRunner"
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
    testOptions {
        unitTests {
            isReturnDefaultValues = true
        }
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
    namespace = "com.kape.vpn_permissions"
}

dependencies {
    coreLibraryDesugaring(Android.desugarJdkLibs)

    implementation(Android.androidCore)
    implementation(Compose.ui)
    implementation(Compose.material)
    implementation(Compose.preview)
    implementation(Android.lifecycle)
    implementation(Android.viewmodel)
    implementation(Compose.viewmodel)
    implementation(Compose.activity)
    implementation(Koin.core)
    implementation(Koin.android)
    implementation(Koin.compose)
    implementation(Compose.navigation)

    implementation(Kotlin.coroutinesCore)
    implementation(Kotlin.coroutinesAndroid)

    implementation(project(mapOf("path" to ":uicomponents")))
    implementation(project(mapOf("path" to ":router")))

    testImplementation(UnitTest.junit)
    testImplementation(UnitTest.coroutines)
    testImplementation(UnitTest.turbine)
    testImplementation(UnitTest.koinTest)
    testImplementation(UnitTest.koinTestJunit5)
    testImplementation(UnitTest.jUnit5Api)
    testRuntimeOnly(UnitTest.jUnit5Engine)
    testImplementation(UnitTest.jUnit5Params)
    testImplementation(UnitTest.mockk)
    testImplementation(UnitTest.androidCoreArch)

    androidTestImplementation(AndroidTest.jUnit)
    androidTestImplementation(AndroidTest.espressoCore)
    androidTestImplementation(AndroidTest.composeUI)
    androidTestImplementation(AndroidTest.mockk)
    androidTestImplementation(AndroidTest.koinTest)
    androidTestImplementation(AndroidTest.androidCore)
    androidTestImplementation(AndroidTest.uiAutomator)

    debugImplementation(DebugTest.composeUI)
    debugImplementation(DebugTest.composeManifest)
}