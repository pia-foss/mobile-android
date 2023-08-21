package com.kape.notifications.data

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.os.Build
import org.koin.core.component.KoinComponent

class NotificationChannelManager(
    private val context: Context
) : KoinComponent {

    fun createVpnChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val notificationChannel =
                NotificationChannel(
                    NOTIFICATION_ID,
                    CHANNEL_ID,
                    NotificationManager.IMPORTANCE_DEFAULT
                )
            notificationChannel.lightColor = Color.BLUE
            notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    companion object {
        const val CHANNEL_ID = "pia"
        const val CHANNEL_NAME = "pia-vpn"
        const val NOTIFICATION_ID = "vpn"
    }
}