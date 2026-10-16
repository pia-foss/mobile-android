package com.kape.ui.utils

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation3.runtime.NavKey

val LocalColors = staticCompositionLocalOf<ColorScheme> { error("Colors Not Provided") }

// True when the app-level navigation rail is shown, so screens can hide their own menu entry points
val LocalNavigationSuiteVisible = compositionLocalOf { false }

// True when the screen is shown as the detail pane beside its list, so it has no back navigation
// of its own: the list stays visible and is the way to move on
val LocalIsDetailPane = compositionLocalOf { false }

// The item open in the detail pane while a list is shown beside it, so the list can mark it;
// null when the list is a full screen
val LocalSelectedListItem = compositionLocalOf<NavKey?> { null }