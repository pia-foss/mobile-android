import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.application) apply false
    alias(libs.plugins.library) apply false
    alias(libs.plugins.kotlin) apply false
    alias(libs.plugins.serialization) apply false
    alias(libs.plugins.compose) apply false
    alias(libs.plugins.jvm) apply false
    alias(libs.plugins.ktlint) apply false
    id("com.github.ben-manes.versions") version "0.53.0"
}

tasks.named<DependencyUpdatesTask>("dependencyUpdates").configure {
    // Enables checking all versions in libs.versions.toml
    checkConstraints = true
    checkBuildEnvironmentConstraints = true

    // Optional: check Gradle version too
    checkForGradleUpdate = true

    // Optional: only show stable releases
    rejectVersionIf { isNonStable(candidate.version) }
    revision = "release"
}

subprojects {
    plugins.withId("com.android.application") {
        apply(plugin = "configuration")
    }
    plugins.withId("com.android.library") {
        apply(plugin = "configuration")
    }
}

fun isNonStable(version: String): Boolean {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.uppercase().contains(it) }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    val isStable = stableKeyword || regex.matches(version)
    return isStable.not()
}

