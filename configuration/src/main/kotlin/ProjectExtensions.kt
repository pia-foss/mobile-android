import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType

internal val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

internal fun Project.applyPlugin(alias: String) {
    pluginManager.apply(libs.findPlugin(alias).get().get().pluginId)
}

internal fun VersionCatalog.bundle(alias: String) = findBundle(alias).get()
