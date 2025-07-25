// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.application) apply false
    alias(libs.plugins.library) apply false
    alias(libs.plugins.kotlin) apply false
    alias(libs.plugins.serialization) apply false
    alias(libs.plugins.compose) apply false
    alias(libs.plugins.jvm) apply false
    alias(libs.plugins.ktlint) apply false
    alias(libs.plugins.junit5) apply false
}

subprojects {
    plugins.withId("com.android.application") {
        apply(plugin = "configuration")
    }
    plugins.withId("com.android.library") {
        apply(plugin = "configuration")
    }
}
