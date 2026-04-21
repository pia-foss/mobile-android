package com.kape.ui.theme

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
import com.kape.contracts.Router
import com.kape.data.DestinationNavOptions
import com.kape.router.LocalNavigator
import com.kape.router.Navigator
import com.kape.ui.utils.LocalColors
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun PIATheme(
    isTv: Boolean,
    darkTheme: Boolean = isDarkTheme(isTv),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme =
        when {
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
    isTv: Boolean,
    darkTheme: Boolean = isDarkTheme(isTv),
    router: Router,
    vararg compositionLocalValues: ProvidedValue<*>,
    content: @Composable (navController: NavHostController) -> Unit,
) {
    val navController = rememberNavController()

    val materialColorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val piaColorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val navigator =
        remember(router) {
            Navigator(router = router)
        }

    val providedValues =
        remember(navigator, piaColorScheme, compositionLocalValues) {
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

            LaunchedEffect(navController) {
                // Suspend until NavHost has set the graph
                snapshotFlow {
                    try {
                        navController.graph
                        true
                    } catch (e: IllegalStateException) {
                        false
                    }
                }.first { it }

                launch {
                    router
                        .getNavigationState()
                        .filter { it != null }
                        .collect { destination ->
                            val options = destination!!.navOptions
                            navController.navigate(destination) {
                                when (options) {
                                    is DestinationNavOptions.None -> { // no-op
                                    }

                                    is DestinationNavOptions.PopUpTo -> {
                                        popUpTo(options.destination) {
                                            inclusive = options.inclusive
                                        }
                                    }

                                    DestinationNavOptions.ClearAll -> {
                                        popUpTo(navController.graph.id) {
                                            inclusive = true
                                        }
                                        launchSingleTop = true
                                    }
                                }
                            }
                            router.resetNavigation()
                        }
                }

                launch {
                    router
                        .getBackState()
                        .filter { it }
                        .collect {
                            navController.previousBackStackEntry != null && navController.popBackStack()
                            router.resetBack()
                        }
                }
            }
        }
    }
}

@Composable
private fun isDarkTheme(isTv: Boolean) =
    if (isTv) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        true
    } else {
        isSystemInDarkTheme()
    }