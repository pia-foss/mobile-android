plugins {
    alias(libs.plugins.library)
    alias(libs.plugins.kotlin)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.junit5)
}

android {
    namespace = "com.kape.csi"
    compileSdk = 35

    defaultConfig {
        minSdk = 24
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(libs.kape.csi)
    implementation(libs.bundles.koin)
    implementation(libs.coroutines)
    testImplementation(libs.bundles.test)
    testImplementation(libs.bundles.kointest)
    testImplementation(libs.coroutines.test)
}