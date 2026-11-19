import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) =
        with(target) {
            pluginManager.apply("pia.android.library.compose")

            dependencies {
                add("implementation", project(":capabilities:ui"))
                add("implementation", libs.bundle("android-bundle"))
                add("implementation", libs.bundle("serialization-bundle"))
            }
        }
}
