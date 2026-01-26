plugins {
    alias(libs.plugins.library)
    alias(libs.plugins.configuration)
    alias(libs.plugins.ktlint)
}

android {
    namespace = "com.kape.signup"
}

dependencies {
    implementation(project(":core:utils"))
}