package com.kape.location.mocklocation

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

/**
 * Builds the foreground notification for [MockLocationService].
 *
 * Kept separate from the VPN notification so the mock location service never mutates
 * the shared VPN notification state, and gets its own channel.
 */
class MockLocationNotificationProvider(
    private val context: Context,
    private val smallIconRes: Int,
    private val title: String,
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
            .setSmallIcon(smallIconRes)
            .setContentTitle(title)
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