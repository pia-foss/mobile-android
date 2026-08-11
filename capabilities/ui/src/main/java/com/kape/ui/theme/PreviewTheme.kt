package com.kape.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.kape.contracts.Router
import com.kape.data.ComposeDestination
import com.kape.ui.utils.LocalColors
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.koin.compose.KoinApplicationPreview
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Wraps a screen's stateless content for use in @Preview, reassembling the pieces the real app
 * provides at its root:
 * - PIATheme alone only sets up MaterialTheme - LocalColors is provided separately by PiaScreen
 *   at the app's root, which previews never go through.
 * - The Surface matters too: MainActivity wraps the whole NavHost in one sized to
 *   MaterialTheme.colorScheme.background, which is what threads the theme into the default
 *   (unset) content color that plain Text composables fall back to - without it they stay
 *   pinned to Material3's ambient default (black) regardless of dark mode.
 * - Shared components (e.g. Footer) may pull dependencies via koinInject() directly - the
 *   global Koin app is never started during preview rendering, so KoinApplicationPreview spins
 *   up an isolated one scoped to the composition instead. A no-op Router is provided by
 *   default; pass [additionalModules] for anything else a previewed screen's components need.
 *
 * darkTheme is driven by isSystemInDarkTheme() rather than a parameter so a "Dark" @Preview
 * (uiMode = Configuration.UI_MODE_NIGHT_YES) drives PIATheme, LocalColors and the Surface
 * together - stack a "Light" and a "Dark" @Preview on the same function to cover both.
 */
@Composable
fun PreviewTheme(
    isTv: Boolean = false,
    additionalModules: List<Module> = emptyList(),
    content: @Composable () -> Unit,
) {
    val darkTheme = isSystemInDarkTheme()
    KoinApplicationPreview(application = {
        modules(listOf(previewRouterModule) + additionalModules)
    }) {
        PIATheme(isTv = isTv, darkTheme = darkTheme) {
            CompositionLocalProvider(LocalColors provides if (darkTheme) DarkColorScheme else LightColorScheme) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    content()
                }
            }
        }
    }
}

private val previewRouterModule =
    module {
        single<Router> { PreviewRouter }
    }

private val PreviewRouter =
    object : Router {
        override fun updateDestination(destination: ComposeDestination) {}

        override fun getNavigationState(): StateFlow<ComposeDestination?> = MutableStateFlow(null)

        override fun resetNavigation() {}

        override fun navigateBack() {}

        override fun getBackState(): StateFlow<Boolean> = MutableStateFlow(false)

        override fun resetBack() {}
    }