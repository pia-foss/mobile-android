plugins {
    alias(libs.plugins.library)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.configuration)
}

android {
    namespace = "com.kape.vpnregions"

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
    implementation(project(":core:localprefs:regions:data"))

    implementation(libs.bundles.serialization)
}