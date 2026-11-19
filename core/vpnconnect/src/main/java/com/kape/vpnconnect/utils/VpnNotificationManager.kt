package com.kape.vpnconnect.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.kape.vpnconnect.R
import org.koin.core.annotation.Singleton
import java.util.concurrent.atomic.AtomicReference

// Kept from mobile-android, so the channel users may have customised keeps its settings.
private const val CHANNEL_ID = "pia"
private const val CHANNEL_NAME = "pia-vpn"

private data class NotificationState(
    val title: String = "",
    val text: String? = null,
    val intent: PendingIntent? = null,
)

@Singleton
class VpnNotificationManager(
    private val context: Context,
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
        notificationBuilder.setSmallIcon(R.drawable.ic_stat_pia_robot_white)
        notificationBuilder.setCategory(Notification.CATEGORY_SERVICE)
        notificationBuilder.setOngoing(true)
        return notificationBuilder
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createChannelAndBuilder(): Notification.Builder {
        val notificationChannel =
            NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_MIN,
            )
        notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val service =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(notificationChannel)
        return Notification.Builder(
            context,
            CHANNEL_ID,
        )
    }
}