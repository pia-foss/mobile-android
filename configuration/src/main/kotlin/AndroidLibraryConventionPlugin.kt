import Coverage.configureKover
import org.gradle.api.Plugin
import org.gradle.api.Project

class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) =
        with(target) {
            applyPlugin("android-library")
            applyPlugin("configuration")
            applyPlugin("ktlint")
            applyPlugin("kotlin-serialization")
            applyPlugin("koin-compiler")
            applyPlugin("junit5")
            configureKover()
        }
}
