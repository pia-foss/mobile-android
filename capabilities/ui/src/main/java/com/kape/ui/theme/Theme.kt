package com.kape.ui.theme

import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.kape.router.LocalNavigator
import com.kape.router.Navigator
import com.kape.router.Router
import com.kape.ui.utils.LocalColors
import com.kape.utils.PlatformUtils
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun PIATheme(
    darkTheme: Boolean = isDarkTheme(context = LocalContext.current),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) DarkColorScheme else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content,
    )
}

@Composable
fun PiaScreen(
    darkTheme: Boolean = isDarkTheme(context = LocalContext.current),
    router: Router,
    vararg compositionLocalValues: ProvidedValue<*>,
    content: @Composable (navController: NavHostController) -> Unit,
) {
    val navController = rememberNavController()

    val materialColorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val piaColorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    // Navigator now only writes to Router - never touches navController directly
    val navigator = remember(router) {
        Navigator(router = router)
    }

    val providedValues = remember(navigator, piaColorScheme, compositionLocalValues) {
        buildList {
            addAll(compositionLocalValues)
            add(LocalColors provides piaColorScheme)
            add(LocalNavigator provides navigator)
        }
    }

    CompositionLocalProvider(*providedValues.toTypedArray()) {
        MaterialTheme(
            typography = AppTypography,
            colorScheme = materialColorScheme,
        ) {
            content(navController)

            // Single coroutine: wait for graph, then handle ALL navigation
            LaunchedEffect(navController) {
                // Suspend until NavHost has set the graph
                snapshotFlow {
                    try { navController.graph; true }
                    catch (e: IllegalStateException) { false }
                }.first { it }

                // Graph is ready - merge both forward and back navigation
                launch {
                    router.getNavigationState()
                        .filter { it != null }
                        .collect { destination ->
                            navController.navigate(destination!!)
                            router.resetNavigation()
                        }
                }

                launch {
                    router.getBackState()
                        .filter { it }
                        .collect {
                            navController.popBackStack()
                            router.resetBack()
                        }
                }
            }
        }
    }
}

@Composable
private fun isDarkTheme(context: Context) =
    if (PlatformUtils.isTv(context = context)) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        true
    } else {
        isSystemInDarkTheme()
    }