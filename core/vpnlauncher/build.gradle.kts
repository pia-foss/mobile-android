plugins {
    alias(libs.plugins.library)
    alias(libs.plugins.configuration)
    alias(libs.plugins.koin.compiler)
    alias(libs.plugins.ktlint)
}

android {
    namespace = "com.kape.vpnlauncher"
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
}

dependencies {
    implementation(libs.bundles.koin)
    testImplementation(libs.bundles.kointest)
    androidTestImplementation(libs.bundles.koinandroidtest)
    implementation(project(":core:utils"))
    implementation(project(":core:localprefs:prefs"))
    implementation(project(":core:localprefs:data"))
    implementation(project(":core:vpnconnect"))
    implementation(project(":core:regions"))
}

koinCompiler {
    compileSafety = false
}