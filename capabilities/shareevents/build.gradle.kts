plugins {
    alias(libs.plugins.library)
    alias(libs.plugins.kotlin)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.junit5)
}

android {
    namespace = "com.kape.shareevents"
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

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    coreLibraryDesugaring(libs.desugar)

    implementation(libs.kape.kpi)
    implementation(project(":core:utils"))
    implementation(project(":core:localprefs:shareevents"))
    implementation(project(":core:localprefs:settings"))
    implementation(project(":core:localprefs:settings:data"))

    implementation(libs.bundles.serialization)
    implementation(libs.bundles.koin)
    implementation(libs.coroutines)
    testImplementation(libs.bundles.test)
    testImplementation(libs.bundles.kointest)
    testImplementation(libs.coroutines.test)
}