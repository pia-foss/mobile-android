package com.kape.location.mocklocation

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.kape.location.R
import org.koin.core.annotation.Singleton

@Singleton
class MockLocationNotificationProvider(
    private val context: Context,
) {
    fun build(): Notification {
        val builder =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createChannel()
                Notification.Builder(context, CHANNEL_ID)
            } else {
                @Suppress("DEPRECATION")
                Notification.Builder(context)
            }
        return builder
            .setSmallIcon(R.drawable.ic_stat_pia_robot_white)
            .setContentTitle(context.getString(com.kape.ui.R.string.mock_location_notification_title))
            .setCategory(Notification.CATEGORY_SERVICE)
            .setOngoing(true)
            .build()
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel =
            NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW,
            )
        channel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    companion object {
        const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "pia-mock-location"
        private const val CHANNEL_NAME = "pia-mock-location"
    }
}