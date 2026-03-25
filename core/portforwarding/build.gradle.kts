plugins {
    alias(libs.plugins.library)
    alias(libs.plugins.configuration)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.serialization)
}

android {
    namespace = "com.kape.portforwarding"
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
    implementation(project(":core:httpclient"))
    implementation(project(":core:localprefs:prefs"))
    implementation(project(":core:localprefs:data"))
    implementation(libs.coroutines)
    implementation(libs.bundles.serialization)
    implementation(libs.bundles.ktor)
    implementation(libs.bundles.koin)
    testImplementation(libs.bundles.kointest)
    androidTestImplementation(libs.bundles.koinandroidtest)
    implementation(libs.spongycastle)
}