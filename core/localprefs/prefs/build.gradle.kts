plugins {
    alias(libs.plugins.library)
    alias(libs.plugins.configuration)
    alias(libs.plugins.serialization)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.koin.compiler)
}

android {
    namespace = "com.kape.localprefs.prefs"
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
    implementation(project(":core:utils"))
    implementation(project(":core:localprefs:data"))
    implementation(libs.bundles.koin)
    implementation(libs.bundles.serialization)
    implementation(libs.kape.account)
}