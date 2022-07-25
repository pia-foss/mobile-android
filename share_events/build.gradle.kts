plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("de.mannodermaus.android-junit5") version "1.8.2.0"
}

android {
    compileSdk = 32

    defaultConfig {
        minSdk = 23
        targetSdk = 32

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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
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
        kotlinCompilerExtensionVersion = Compose.COMPOSE_VERSION
    }
    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    namespace = "com.kape.share_events"
}

dependencies {

    implementation(project(mapOf("path" to ":kpi")))
    coreLibraryDesugaring(Android.desugarJdkLibs)

    implementation(Android.androidCore)
    implementation(Compose.ui)
    implementation(Compose.material)
    implementation(Compose.preview)
    implementation(Android.lifecycle)
    implementation(Android.viewmodel)
    implementation(Android.swipeRefresh)
    implementation(Compose.viewmodel)
    implementation(Compose.activity)
    implementation(Compose.navigation)
    implementation(Android.security)
    implementation(Koin.core)
    implementation(Koin.android)
    implementation(Koin.compose)

    implementation(Kotlin.ktor)
    implementation(Kotlin.coroutinesCore)
    implementation(Kotlin.coroutinesAndroid)
    implementation(Kotlin.serializationCore)
    implementation(Kotlin.serializationJson)
    implementation(Kotlin.dateTime)

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

    debugImplementation(DebugTest.composeUI)
    debugImplementation(DebugTest.composeManifest)
}