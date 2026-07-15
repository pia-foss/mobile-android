package com.kape.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.kape.notifications.data.NotificationChannelManager
import java.util.concurrent.atomic.AtomicReference

private data class NotificationState(
    val title: String = "",
    val text: String? = null,
    val intent: PendingIntent? = null,
)

/**
 * Sole owner of the VPN foreground notification's mutable state.
 *
 * The connection, status-update and automation-restart flows each update a different
 * subset of fields from different threads. State changes are merged via [AtomicReference]
 * so callers never mutate a shared [Notification.Builder] concurrently - each call builds
 * a fresh, independent [Notification] from the latest merged snapshot.
 */
class VpnNotificationManager(
    private val context: Context,
    private val smallIconRes: Int,
) {
    private val state = AtomicReference(NotificationState())

    fun updateConnectionInfo(
        title: String,
        intent: PendingIntent,
    ): Notification = build(state.updateAndGet { it.copy(title = title, intent = intent) })

    fun updateStatusText(text: String): Notification = build(state.updateAndGet { it.copy(text = text) })

    fun updateContentIntent(intent: PendingIntent): Notification = build(state.updateAndGet { it.copy(intent = intent) })

    private fun build(current: NotificationState): Notification =
        newBuilder()
            .apply {
                setContentTitle(current.title)
                current.text?.let { setContentText(it) }
                current.intent?.let { setContentIntent(it) }
            }.build()

    private fun newBuilder(): Notification.Builder {
        val notificationBuilder =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createChannelAndBuilder()
            } else {
                Notification.Builder(context)
            }
        notificationBuilder.setSmallIcon(smallIconRes)
        notificationBuilder.setCategory(Notification.CATEGORY_SERVICE)
        notificationBuilder.setOngoing(true)
        return notificationBuilder
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createChannelAndBuilder(): Notification.Builder {
        val notificationChannel =
            NotificationChannel(
                NotificationChannelManager.CHANNEL_ID,
                NotificationChannelManager.CHANNEL_NAME,
                NotificationManager.IMPORTANCE_MIN,
            )
        notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val service =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(notificationChannel)
        return Notification.Builder(
            context,
            NotificationChannelManager.CHANNEL_ID,
        )
    }
}