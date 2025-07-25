import com.android.build.api.dsl.ApplicationDefaultConfig
import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

object Config {
    const val COMPILE_SDK = 35
    const val MIN_SDK = 24
    const val TARGET_SDK = 35
}

class ConfigurationPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.plugins.withId("com.android.application") {
            configureAndroidAppProject(project)
        }
        project.plugins.withId("com.android.library") {
            configureAndroidLibraryProject(project)
        }
    }

    private fun configureAndroidAppProject(project: Project) {
        val androidExt = project.extensions.findByType(ApplicationExtension::class.java)
        androidExt?.applyCommonAndroidConfig(project)
    }

    private fun configureAndroidLibraryProject(project: Project) {
        val androidExt = project.extensions.findByType(LibraryExtension::class.java)
        androidExt?.applyCommonAndroidConfig(project)
    }

    private fun CommonExtension<*, *, *, *, *, *>.applyCommonAndroidConfig(project: Project) {
        compileSdk = Config.COMPILE_SDK

        defaultConfig {
            minSdk = Config.MIN_SDK
            // targetSdk only available for Application modules
            if (this is ApplicationDefaultConfig) {
                targetSdk = Config.TARGET_SDK
            }
        }
        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_17
            targetCompatibility = JavaVersion.VERSION_17
        }

        // Also set Java compatibility for the Java plugin (if applied)
        project.extensions.findByType(JavaPluginExtension::class.java)?.run {
            sourceCompatibility = JavaVersion.VERSION_17
            targetCompatibility = JavaVersion.VERSION_17
        }
        project.tasks.withType<KotlinJvmCompile>().configureEach {
            compilerOptions {
                jvmTarget.set(JvmTarget.JVM_17)
            }
        }
    }
}
