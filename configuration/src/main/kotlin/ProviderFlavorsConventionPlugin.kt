import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

object ProviderFlavor {
    const val DIMENSION = "provider"

    const val AMAZON = "amazon"
    const val GOOGLE = "google"
    const val NO_IN_APP = "noinapp"
    const val META = "meta"
    const val FDROID = "fdroid"

    val ALL = listOf(AMAZON, GOOGLE, NO_IN_APP, META, FDROID)

    val WITH_PLAY_SERVICES = listOf(GOOGLE, NO_IN_APP)
}

class ProviderFlavorsConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) =
        with(target) {
            // AGP has no common type for productFlavors, so the app and library cases are separate.
            plugins.withId("com.android.application") {
                extensions.configure<ApplicationExtension> {
                    flavorDimensions += ProviderFlavor.DIMENSION
                    ProviderFlavor.ALL.forEach { name -> productFlavors.create(name) { dimension = ProviderFlavor.DIMENSION } }
                }
            }
            plugins.withId("com.android.library") {
                extensions.configure<LibraryExtension> {
                    flavorDimensions += ProviderFlavor.DIMENSION
                    ProviderFlavor.ALL.forEach { name -> productFlavors.create(name) { dimension = ProviderFlavor.DIMENSION } }
                }
            }
        }
}

fun CommonExtension.shareFlavorSources(
    directory: String,
    flavors: List<String>,
) {
    flavors.forEach { flavor -> sourceSets.getByName(flavor).kotlin.directories += "src/$directory/java" }
}