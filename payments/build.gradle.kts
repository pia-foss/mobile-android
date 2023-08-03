plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("kotlinx-serialization")
    id("de.mannodermaus.android-junit5") version "1.8.2.0"
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

    flavorDimensions.add("provider")
    productFlavors {
        create("amazon") {
            dimension = "provider"
        }
        create("google") {
            dimension = "provider"
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
    namespace = "com.kape.payments"
}

dependencies {

    implementation(project(mapOf("path" to ":account")))
    implementation(project(mapOf("path" to ":core")))
    coreLibraryDesugaring(Android.desugarJdkLibs)

    implementation(Android.androidCore)
    implementation(Compose.ui)
    implementation(Compose.material)
    implementation(Compose.preview)
    implementation(Android.lifecycle)
    implementation(Android.viewmodel)
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

    "amazonImplementation"(Payments.amazon)
    "googleImplementation"(Payments.google)

    testImplementation(UnitTest.junit)
    testImplementation(UnitTest.coroutines)
    testImplementation(UnitTest.turbine)
    testImplementation(UnitTest.koinTest)
    testImplementation(UnitTest.koinTestJunit5)
    testImplementation(UnitTest.jUnit5Api)
    testRuntimeOnly(UnitTest.jUnit5Engine)
    testImplementation(UnitTest.jUnit5Params)
    testImplementation(UnitTest.mockk)
}