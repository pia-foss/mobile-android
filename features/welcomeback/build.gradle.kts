plugins {
    alias(libs.plugins.library)
    alias(libs.plugins.configuration)
    alias(libs.plugins.koin.compiler)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.junit5)
    alias(libs.plugins.compose)
}

android {
    namespace = "com.kape.welcomeback"

    testOptions {
        unitTests {
            isReturnDefaultValues = true
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
        create("noinapp") {
            dimension = "provider"
        }
        create("meta") {
            dimension = "provider"
        }
        create("fdroid") {
            dimension = "provider"
        }
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":core:contracts"))
    implementation(project(":core:data"))
    implementation(project(":core:payments"))
    implementation(project(":core:utils"))
    implementation(project(":capabilities:ui"))
    implementation(project(":capabilities:shareevents"))
    implementation(project(":features:signup"))
    implementation(project(":features:login"))
    implementation(project(":features:permissions"))

    implementation(libs.bundles.compose)
    androidTestImplementation(libs.bundles.composeandroidtest)
    implementation(libs.bundles.android)
    implementation(libs.mobile.shared.kpi)
    implementation(libs.bundles.koin)
    testImplementation(libs.bundles.kointest)
    androidTestImplementation(libs.bundles.koinandroidtest)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.bundles.test)
    runtimeOnly(libs.launcher)
}

koinCompiler {
    compileSafety = false
}