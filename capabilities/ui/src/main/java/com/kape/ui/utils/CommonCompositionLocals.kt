package com.kape.ui.utils

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf

val LocalColors = staticCompositionLocalOf<ColorScheme> { error("Colors Not Provided") }

// True when the app-level navigation rail is shown, so screens can hide their own menu entry points
val LocalNavigationSuiteVisible = compositionLocalOf { false }