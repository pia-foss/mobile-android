package com.kape.ui.utils

import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.WindowSizeClass.Companion.HEIGHT_DP_EXPANDED_LOWER_BOUND
import androidx.window.core.layout.WindowSizeClass.Companion.HEIGHT_DP_MEDIUM_LOWER_BOUND
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_EXPANDED_LOWER_BOUND
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_MEDIUM_LOWER_BOUND

enum class DeviceConfiguration {
    MOBILE_PORTRAIT,
    MOBILE_LANDSCAPE,
    TABLET_PORTRAIT,
    TABLET_LANDSCAPE,
    DESKTOP,
    ;

    companion object {
        fun fromWindowSizeClass(windowSizeClass: WindowSizeClass): DeviceConfiguration {
            val widthAtLeastMedium =
                windowSizeClass.isWidthAtLeastBreakpoint(WIDTH_DP_MEDIUM_LOWER_BOUND)
            val widthAtLeastExpanded =
                windowSizeClass.isWidthAtLeastBreakpoint(WIDTH_DP_EXPANDED_LOWER_BOUND)
            val heightAtLeastMedium =
                windowSizeClass.isHeightAtLeastBreakpoint(HEIGHT_DP_MEDIUM_LOWER_BOUND)
            val heightAtLeastExpanded =
                windowSizeClass.isHeightAtLeastBreakpoint(HEIGHT_DP_EXPANDED_LOWER_BOUND)

            val isWidthCompact = !widthAtLeastMedium
            val isWidthMedium = widthAtLeastMedium && !widthAtLeastExpanded

            val isHeightCompact = !heightAtLeastMedium
            val isHeightMedium = heightAtLeastMedium && !heightAtLeastExpanded

            return when {
                isWidthCompact && (isHeightMedium || heightAtLeastExpanded) -> MOBILE_PORTRAIT
                widthAtLeastExpanded && isHeightCompact -> MOBILE_LANDSCAPE
                isWidthMedium && heightAtLeastExpanded -> TABLET_PORTRAIT
                widthAtLeastExpanded && isHeightMedium -> TABLET_LANDSCAPE
                else -> DESKTOP
            }
        }
    }
}