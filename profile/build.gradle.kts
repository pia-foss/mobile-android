plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("de.mannodermaus.android-junit5") version "1.8.2.0"
}

android {
    compileSdk = 33

    defaultConfig {
        minSdk = 21
        targetSdk = 33

        testInstrumentationRunner = "com.kape.profile.AndroidTestRunner"
        consumerProguardFiles("consumer-rules.pro")

        buildConfigField("int", "VERSION_CODE", "1")
        buildConfigField("String", "VERSION_NAME", "\"1.0\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        // Flag to enable support for the new language APIs
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
    android.buildFeatures.buildConfig = true
    namespace = "com.kape.profile"
}

dependencies {
    implementation(project(mapOf("path" to ":account")))
    implementation(project(mapOf("path" to ":core")))
    implementation(project(mapOf("path" to ":uicomponents")))
    implementation(project(mapOf("path" to ":router")))

    coreLibraryDesugaring(Android.desugarJdkLibs)

    implementation(Android.androidCore)

    implementation(Compose.ui)
    implementation(Compose.material)
    implementation(Compose.preview)
    implementation(Compose.viewmodel)

    implementation(Koin.core)
    implementation(Koin.android)
    implementation(Koin.compose)

    implementation(Kotlin.coroutinesCore)
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