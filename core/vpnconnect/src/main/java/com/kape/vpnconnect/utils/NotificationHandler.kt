package com.kape.vpnconnect.utils

import android.app.Notification
import android.app.NotificationManager
import com.kape.utils.NOTIFICATION_ID

class NotificationHandler(
    private val notificationManager: NotificationManager,
    private val notificationBuilder: Notification.Builder,
) {
    fun update(status: String) {
        notificationBuilder.setContentText(status)
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
    }
}