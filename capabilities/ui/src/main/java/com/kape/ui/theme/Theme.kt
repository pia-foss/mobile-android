package com.kape.ui.theme

import android.app.Activity
import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.kape.ui.utils.LocalColors
import com.kape.utils.PlatformUtils

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
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
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
    vararg compositionLocalValues: ProvidedValue<*>,
    content: @Composable () -> Unit,
) {
    val materialColorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val piaColorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val providedValues = buildList {
        addAll(compositionLocalValues)
        add(LocalColors provides piaColorScheme)
    }

    CompositionLocalProvider(*providedValues.toTypedArray()) {
        MaterialTheme(
            typography = AppTypography,
            colorScheme = materialColorScheme,
            content = content,
        )
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