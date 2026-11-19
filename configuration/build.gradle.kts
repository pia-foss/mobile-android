import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `kotlin-dsl`
}

repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    compileOnly(libs.kotlin.gradle.plugin)
    compileOnly(libs.android.gradle.plugin)
    compileOnly(libs.kover.gradle.plugin)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

kotlin {
    jvmToolchain(21)
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

gradlePlugin {
    plugins {
        create("configuration") {
            id = "configuration"
            implementationClass = "ConfigurationPlugin"
        }
        create("androidLibrary") {
            id = "pia.android.library"
            implementationClass = "AndroidLibraryConventionPlugin"
        }
        create("androidComposeLibrary") {
            id = "pia.android.library.compose"
            implementationClass = "AndroidComposeLibraryConventionPlugin"
        }
        create("androidFeature") {
            id = "pia.android.feature"
            implementationClass = "AndroidFeatureConventionPlugin"
        }
        create("providerFlavors") {
            id = "pia.android.flavors"
            implementationClass = "ProviderFlavorsConventionPlugin"
        }
        create("androidApplication") {
            id = "pia.android.application"
            implementationClass = "AndroidApplicationConventionPlugin"
        }
    }
}
