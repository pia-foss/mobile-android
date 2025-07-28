plugins {
    id("java-library")
    alias(libs.plugins.jvm)
    alias(libs.plugins.serialization)
}

dependencies {
    implementation(libs.bundles.serialization)
}