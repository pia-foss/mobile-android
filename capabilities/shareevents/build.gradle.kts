plugins {
    alias(libs.plugins.library)
    alias(libs.plugins.kotlin)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.junit5)
    alias(libs.plugins.configuration)
}

android {
    namespace = "com.kape.shareevents"

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