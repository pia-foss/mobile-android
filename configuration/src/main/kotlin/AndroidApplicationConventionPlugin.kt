import Coverage.configureKover
import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) =
        with(target) {
            applyPlugin("android-application")
            applyPlugin("configuration")
            applyPlugin("ktlint")
            applyPlugin("kotlin-compose")
            applyPlugin("koin-compiler")
            applyPlugin("junit5")
            configureKover()
            pluginManager.apply("pia.android.flavors")

            extensions.configure<ApplicationExtension> {
                buildFeatures {
                    compose = true
                }
            }

            dependencies {
                add("debugImplementation", libs.bundle("compose-debug-bundle"))
            }
        }
}
