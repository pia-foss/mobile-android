package com.kape.vpnconnect.utils

import android.app.NotificationManager
import android.app.PendingIntent
import com.kape.data.NOTIFICATION_ID
import com.kape.utils.VpnNotificationManager

class NotificationHandler(
    private val notificationManager: NotificationManager,
    private val vpnNotificationManager: VpnNotificationManager,
) {
    fun update(status: String) {
        notificationManager.notify(NOTIFICATION_ID, vpnNotificationManager.updateStatusText(status))
    }

    fun updateConnectionInfo(
        title: String,
        intent: PendingIntent,
    ) {
        notificationManager.notify(NOTIFICATION_ID, vpnNotificationManager.updateConnectionInfo(title, intent))
    }
}