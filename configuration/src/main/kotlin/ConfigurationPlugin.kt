import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

object Config {
    const val COMPILE_SDK = 37
    const val MIN_SDK = 24
    const val TARGET_SDK = 37
}

class ConfigurationPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.plugins.withId("com.android.application") {
            project.extensions.findByType<ApplicationExtension>()
                ?.applyAndroidConfig(project)
        }

        project.plugins.withId("com.android.library") {
            project.extensions.findByType<LibraryExtension>()
                ?.applyAndroidConfig(project)
        }
    }

    /** Shared config for all modules */
    private fun CommonExtension.applySharedConfig(project: Project) {
        compileSdk = Config.COMPILE_SDK

        // Kotlin JVM target for all modules
        project.tasks.withType<KotlinJvmCompile>().configureEach {
            compilerOptions.jvmTarget.set(JvmTarget.JVM_21)
        }

        // Java plugin compatibility (if applied)
        project.extensions.findByType<JavaPluginExtension>()?.apply {
            sourceCompatibility = JavaVersion.VERSION_21
            targetCompatibility = JavaVersion.VERSION_21
        }
    }

    /** App-specific config */
    private fun ApplicationExtension.applyAndroidConfig(project: Project) {
        applySharedConfig(project)

        defaultConfig {
            minSdk = Config.MIN_SDK
            targetSdk = Config.TARGET_SDK
        }

        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_21
            targetCompatibility = JavaVersion.VERSION_21
        }
    }

    /** Library-specific config */
    private fun LibraryExtension.applyAndroidConfig(project: Project) {
        applySharedConfig(project)

        defaultConfig {
            minSdk = Config.MIN_SDK
        }

        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_21
            targetCompatibility = JavaVersion.VERSION_21
        }
    }
}
