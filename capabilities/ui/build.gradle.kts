plugins {
    alias(libs.plugins.library)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.compose)
    alias(libs.plugins.configuration)
}

android {
    namespace = "com.kape.ui"
    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    coreLibraryDesugaring(libs.desugar)

    implementation(project(":core:router"))
    implementation(project(":core:utils"))

    implementation(libs.bundles.compose)
    implementation(libs.bundles.koin)
    androidTestImplementation(libs.bundles.koinandroidtest)
    androidTestImplementation(libs.bundles.composeandroidtest)
    implementation(libs.bundles.swipetoreferesh)
    testImplementation(libs.bundles.test)
    testImplementation(libs.bundles.kointest)
    testImplementation(libs.coroutines.test)
    runtimeOnly(libs.launcher)
}

tasks.withType<Test> {
    useJUnitPlatform()
}