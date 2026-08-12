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