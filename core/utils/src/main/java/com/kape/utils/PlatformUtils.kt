package com.kape.utils

import android.content.Context
import android.content.pm.PackageManager

object PlatformUtils {

    fun isTv(context: Context): Boolean {
        when {
            context.packageManager.hasSystemFeature(PackageManager.FEATURE_TELEVISION) ||
                context.packageManager.hasSystemFeature(PackageManager.FEATURE_LEANBACK) ||
                context.packageManager.hasSystemFeature(PackageManager.FEATURE_LIVE_TV) ||
                context.packageManager.hasSystemFeature("amazon.hardware.fire_tv") -> {
                return true
            }
        }
        return false
    }
}