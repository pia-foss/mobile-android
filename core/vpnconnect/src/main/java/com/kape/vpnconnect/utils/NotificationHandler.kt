package com.kape.vpnconnect.utils

import android.app.Notification
import android.app.NotificationManager
import com.kape.data.NOTIFICATION_ID

class NotificationHandler(
    private val notificationManager: NotificationManager,
    private val notificationBuilder: Notification.Builder,
) {
    fun update(status: String) {
        // notificationBuilder is a shared singleton also mutated from other threads
        // (see ConnectionConfigurationUseCaseImpl); synchronize to avoid racing writes.
        val notification =
            synchronized(notificationBuilder) {
                notificationBuilder.setContentText(status)
                notificationBuilder.build()
            }
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}