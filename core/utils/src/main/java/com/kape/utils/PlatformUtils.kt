package com.kape.utils

import android.app.UiModeManager
import android.content.Context
import android.content.res.Configuration
import org.koin.core.annotation.Singleton

@Singleton
class PlatformUtils(private val context: Context) {

    fun isTv(): Boolean {
        val uiModeManager =
            context.getSystemService(Context.UI_MODE_SERVICE) as UiModeManager
        return uiModeManager.currentModeType == Configuration.UI_MODE_TYPE_TELEVISION
    }

    fun isTablet(): Boolean {
        return context.resources.configuration.smallestScreenWidthDp >= 600
    }
}