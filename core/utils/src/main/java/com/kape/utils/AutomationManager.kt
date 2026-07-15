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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.applicationContext.startForegroundService(automationServiceIntent)
        } else {
            context.applicationContext.startService(automationServiceIntent)
        }
    }

    fun stopAutomationService() {
        context.applicationContext.stopService(automationServiceIntent)
    }
}