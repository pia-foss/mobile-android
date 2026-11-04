// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.application) apply false
    alias(libs.plugins.library) apply false
    alias(libs.plugins.serialization) apply false
    alias(libs.plugins.compose) apply false
    alias(libs.plugins.jvm) apply false
    alias(libs.plugins.ktlint) apply false
}

subprojects {
    pluginManager.withPlugin("com.android.application") {
        pluginManager.apply("configuration")
    }
    pluginManager.withPlugin("com.android.library") {
        pluginManager.apply("configuration")
    }
}
