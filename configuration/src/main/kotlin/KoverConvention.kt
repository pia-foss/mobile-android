import kotlinx.kover.gradle.plugin.dsl.KoverProjectExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

object Coverage {
    // Shared by every module and the root project, which merges the modules' variants into one report.
    const val VARIANT = "coverage"

    private val ANDROID_VARIANTS = arrayOf("googleDebug", "debug")

    internal fun Project.configureKover() {
        applyPlugin("kover")
        extensions.configure<KoverProjectExtension> {
            currentProject {
                createVariant(VARIANT) {
                    // A module has either googleDebug (flavored) or debug (not flavored), never both.
                    add(*ANDROID_VARIANTS, optional = true)
                }
            }
        }
    }
}