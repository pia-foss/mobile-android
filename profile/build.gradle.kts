plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    compileSdk = 32

    defaultConfig {
        minSdk = 21
        targetSdk = 32

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
    namespace = "com.kape.profile"
}

dependencies {

    coreLibraryDesugaring(Android.desugar)

    implementation(Android.androidCore)

    implementation(Compose.ui)
    implementation(Compose.material)
    implementation(Compose.preview)

    implementation(Koin.core)
    implementation(Koin.android)
    implementation(Koin.compose)

    implementation(Kotlin.coroutinesCore)

    implementation(project(mapOf("path" to ":account")))
    implementation(project(mapOf("path" to ":core")))

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