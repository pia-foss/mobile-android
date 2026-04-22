plugins {
    alias(libs.plugins.library)
    alias(libs.plugins.configuration)
    alias(libs.plugins.koin.compiler)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.junit5)
    alias(libs.plugins.compose)
}

android {
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
    }
    namespace = "com.kape.connection"

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
    }
    testOptions {
        unitTests {
            isReturnDefaultValues = true
        }
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    coreLibraryDesugaring(libs.desugar)

    implementation(project(":capabilities:ui"))
    implementation(project(":capabilities:notifications"))
    implementation(project(":capabilities:shareevents"))
    implementation(project(":capabilities:snooze"))
    implementation(project(":core:contracts"))
    implementation(project(":core:regions"))
    implementation(project(":core:vpnconnect"))
    implementation(project(":core:portforwarding"))
    implementation(project(":core:localprefs:prefs"))
    implementation(project(":core:localprefs:data"))
    implementation(project(":core:utils"))
    implementation(project(":core:data"))
    implementation(project(":features:appbar"))
    implementation(project(":features:settings"))
    implementation(project(":features:sidemenu"))
    implementation(project(":features:vpnregionselection"))
    implementation(project(":features:dedicatedip"))
    implementation(project(":features:rating"))
    implementation(project(":features:permissions"))
    implementation(project(":capabilities:buildconfig"))
    implementation(project(":capabilities:sharedui"))

    implementation(libs.bundles.compose)
    androidTestImplementation(libs.bundles.composeandroidtest)
    implementation(libs.bundles.android)
    implementation(libs.bundles.koin)
    testImplementation(libs.bundles.kointest)
    androidTestImplementation(libs.bundles.koinandroidtest)
    implementation(libs.coroutines)
    testImplementation(libs.coroutines.test)
    androidTestImplementation(libs.bundles.coroutines.androidtest)
    testImplementation(libs.bundles.test)
    runtimeOnly(libs.launcher)
    implementation(libs.kape.vpnmanager)
    implementation(libs.kape.account)
    implementation(libs.bundles.serialization)
    implementation(libs.constraintlayout)
}

koinCompiler {
    compileSafety = false
}