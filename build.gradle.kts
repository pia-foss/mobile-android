plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.koin.compiler) apply false
    alias(libs.plugins.ktlint) apply false
    alias(libs.plugins.junit5) apply false
    alias(libs.plugins.kover)
}

dependencies {
    // Folders like :core only group modules and have no build file, so there's nothing to measure in them.
    subprojects.filter { it.buildFile.exists() }.forEach { add("kover", it) }
}

kover {
    currentProject {
        // Same name as the variant each module creates in the convention plugins, so they're merged here.
        createVariant("coverage") {}
    }
    reports {
        filters {
            excludes {
                classes(
                    "*.BuildConfig",
                    "*.R",
                    "*.R$*",
                    "*ComposableSingletons*",
                    "com.kape.ui.theme.*",
                    "*.AppNavigationKt*",
                    "com.kape.widget.Widget",
                    "com.kape.widget.Widget$*",
                    "com.kape.widget.WidgetColors*",
                )
                annotatedBy(
                    // Composables aren't unit tested for now; ViewModels in the ui packages are still measured.
                    "androidx.compose.runtime.Composable",
                    "androidx.compose.ui.tooling.preview.Preview",
                    "androidx.compose.ui.tooling.preview.PreviewLightDark",
                    "org.koin.core.annotation.Module",
                )
            }
        }
    }
}