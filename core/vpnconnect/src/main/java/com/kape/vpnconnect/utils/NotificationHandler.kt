package com.kape.vpnconnect.utils

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import com.kape.data.NOTIFICATION_ID
import org.koin.core.annotation.Singleton

@Singleton
class NotificationHandler(
    context: Context,
    private val vpnNotificationManager: VpnNotificationManager,
) {
    private val notificationManager = context.getSystemService(NotificationManager::class.java)

    fun update(status: String) {
        notificationManager.notify(NOTIFICATION_ID, vpnNotificationManager.updateStatusText(status))
    }

    fun updateConnectionInfo(
        title: String,
        intent: PendingIntent,
    ) {
        notificationManager.notify(NOTIFICATION_ID, vpnNotificationManager.updateConnectionInfo(title, intent))
    }

    fun cancel() {
        notificationManager.cancel(NOTIFICATION_ID)
    }
}