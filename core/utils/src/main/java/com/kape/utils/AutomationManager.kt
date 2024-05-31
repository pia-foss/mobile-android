package com.kape.utils

import android.app.Notification
import android.content.Context
import android.content.Intent
import android.os.Build

class AutomationManager(
    private val context: Context,
    private val automationServiceIntent: Intent,
    val notificationBuilder: Notification.Builder,
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