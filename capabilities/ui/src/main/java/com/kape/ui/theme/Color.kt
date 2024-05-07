package com.kape.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

private val darkGreen = Color(0xFF037900)
private val lightGreen = Color(0xFF5DDF5A)
private val white = Color(0xFFFFFFFF)
private val grey20 = Color(0xFF323642)
private val grey25 = Color(0xFF454557)
private val grey30 = Color(0xFF5C6370)
private val grey40 = Color(0xFF889099)
private val grey50 = Color(0xFFA8ADB3)
private val grey70 = Color(0xFFD7D8D9)
private val grey90 = Color(0xFFEEEEEE)
private val overlaid = Color(0x661B1D22)

private val error10 = Color(0xFFB0024C)
private val error30 = Color(0xFFFF72A5)
private val error50 = Color(0xFFFEF1F5)
private val success10 = Color(0xFF037900)
private val success30 = Color(0xFF88E47B)
private val success50 = Color(0xFFD9F6D5)
private val warning10 = Color(0xFF943511)
private val warning30 = Color(0xFFFEA754)
private val warning50 = Color(0xFFFEE4D3)
private val info10 = Color(0xFF0171C4)
private val info30 = Color(0xFF86D0FD)
private val info50 = Color(0xFFEDF5FE)

private val clientRed = Color(0xfff24458)
private val clientDarkYellow = Color(0xFFE6B400)

val LightColorScheme = lightColorScheme(
    primary = darkGreen,
    onPrimary = white,
    primaryContainer = Color.Unspecified,
    onPrimaryContainer = grey40,
    inversePrimary = Color.Unspecified,
    secondary = Color.Unspecified,
    onSecondary = Color.Unspecified,
    secondaryContainer = Color.Unspecified,
    onSecondaryContainer = Color.Unspecified,
    tertiary = Color.Unspecified,
    onTertiary = grey20,
    tertiaryContainer = Color.Unspecified,
    onTertiaryContainer = Color.Unspecified,
    background = grey90,
    onBackground = grey20,
    surface = grey90,
    onSurface = grey20,
    surfaceVariant = white,
    onSurfaceVariant = grey30,
    inverseSurface = grey20,
    inverseOnSurface = grey90,
    error = error10,
    onError = white,
    errorContainer = error50,
    onErrorContainer = error10,
    outline = grey70,
    outlineVariant = grey40,
)

val DarkColorScheme = darkColorScheme(
    primary = lightGreen,
    onPrimary = grey20,
    primaryContainer = grey30,
    onPrimaryContainer = grey25,
    inversePrimary = Color.Unspecified,
    secondary = Color.Unspecified,
    onSecondary = Color.Unspecified,
    secondaryContainer = Color.Unspecified,
    onSecondaryContainer = Color.Unspecified,
    tertiary = Color.Unspecified,
    onTertiary = grey20,
    tertiaryContainer = Color.Unspecified,
    onTertiaryContainer = Color.Unspecified,
    background = grey20,
    onBackground = grey90,
    surface = grey20,
    onSurface = grey90,
    surfaceVariant = grey25,
    onSurfaceVariant = grey70,
    inverseSurface = grey90,
    inverseOnSurface = grey20,
    error = error30,
    onError = white,
    errorContainer = error50,
    onErrorContainer = error10,
    outline = grey40,
    outlineVariant = grey70,
)

fun ColorScheme.defaultGradient(scheme: ColorScheme): List<Color> {
    return listOf(scheme.surface, scheme.surface)
}

fun ColorScheme.connectedGradient(): List<Color> {
    return listOf(Color(0xff4cb649), Color(0xff5ddf5a))
}

fun ColorScheme.connectingGradient(): List<Color> {
    return listOf(Color(0xffe6b400), Color(0xfff9cf01))
}

fun ColorScheme.errorGradient(): List<Color> {
    return listOf(Color(0xffb2352d), Color(0xfff24458))
}

fun ColorScheme.warning30() = warning30

fun ColorScheme.statusBarDefault(scheme: ColorScheme) = scheme.surface
fun ColorScheme.statusBarConnected() = Color(0xff4cb649)
fun ColorScheme.statusBarConnecting() = Color(0xffe6b400)
fun ColorScheme.statusBarError() = Color(0xffb2352d)

fun ColorScheme.errorOutline() = error30
fun ColorScheme.errorBackground() = error50

fun ColorScheme.warningOutline() = warning30
fun ColorScheme.warningBackground() = warning50

fun ColorScheme.infoOutline() = info30
fun ColorScheme.infoBackground() = info50
fun ColorScheme.infoBlue() = info10

fun ColorScheme.successOutline() = success30
fun ColorScheme.successBackground() = success50

fun ColorScheme.connectionDefault() = clientDarkYellow
fun ColorScheme.connectionError() = clientRed

private fun latencyGreen(): Color = Color(0xff4cb649)
private fun latencyYellow(): Color = Color(0xffe6b400)
private fun latencyRed(): Color = Color(0xffb2352d)

fun ColorScheme.getLatencyColor(latency: String?): Color {
    if (latency == null) {
        return Color.White
    }
    return when (latency.toLong()) {
        in 0..99 -> latencyGreen()
        in 100..249 -> latencyYellow()
        else -> latencyRed()
    }
}