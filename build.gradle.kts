import Dependencies.isNonStable
import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.9.0" apply false
    id("com.android.library") version "8.9.0" apply false
    id("org.jetbrains.kotlin.android") version "2.1.10" apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "2.1.10" apply false
    id("org.jlleitschuh.gradle.ktlint") version "11.5.1" apply false
    id("org.jetbrains.kotlin.jvm") version "2.1.10" apply false
    id("com.github.ben-manes.versions") version "0.51.0"
    id("org.jetbrains.kotlinx.kover") version "0.7.5"
    id("org.jetbrains.kotlin.plugin.compose") version "2.1.10"
}

allprojects {
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            jvmTarget = "17"
        }
    }
    tasks.withType<DependencyUpdatesTask> {
        rejectVersionIf {
            isNonStable(candidate.version)
        }
    }
}
