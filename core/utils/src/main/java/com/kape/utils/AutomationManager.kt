package com.kape.utils

import android.content.Context
import android.content.Intent
import android.os.Build

class AutomationManager(
    private val context: Context,
    private val automationServiceIntent: Intent,
    val vpnNotificationManager: VpnNotificationManager,
) {
    fun startAutomationService() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.applicationContext.startForegroundService(automationServiceIntent)
            } else {
                context.applicationContext.startService(automationServiceIntent)
            }
        } catch (e: IllegalStateException) {
            // Starting a foreground service from the background is not allowed on Android 12+.
            // The service is started again on the next app launch, boot or app update.
            e.printStackTrace()
        }
    }

    fun stopAutomationService() {
        context.applicationContext.stopService(automationServiceIntent)
    }
}