plugins {
    alias(libs.plugins.library)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.compose)
    alias(libs.plugins.configuration)
}

android {
    namespace = "com.kape.sharedui"

    buildFeatures {
        compose = true
    }
}

dependencies {
    coreLibraryDesugaring(libs.desugar)

    implementation(project(":core:data"))
    implementation(project(":capabilities:ui"))

    implementation(libs.bundles.compose)
    implementation(libs.bundles.koin)
}

tasks.withType<Test> {
    useJUnitPlatform()
}