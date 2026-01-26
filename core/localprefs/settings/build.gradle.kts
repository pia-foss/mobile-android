plugins {
    alias(libs.plugins.library)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.configuration)
}

android {
    namespace = "com.kape.settings.prefs"
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
    }
}

dependencies {
    coreLibraryDesugaring(libs.desugar)
    implementation(project(":core:utils"))
    implementation(project(":core:localprefs:settings:data"))
    implementation(libs.bundles.serialization)
}