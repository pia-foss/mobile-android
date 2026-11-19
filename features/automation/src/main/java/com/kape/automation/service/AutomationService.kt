package com.kape.automation.service

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import com.kape.automation.data.NetworkMonitor
import com.kape.contracts.di.Qualifiers
import com.kape.vpnconnect.utils.VpnNotificationManager
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject
import org.koin.core.qualifier.named

// The VPN's notification id, so automation and the VPN share one notification.
private const val NOTIFICATION_ID = 123

class AutomationService :
    Service(),
    KoinComponent {
    private val notificationManager: VpnNotificationManager by inject()
    private val notificationIntent: PendingIntent by inject(named(Qualifiers.VPN_NOTIFICATION_INTENT))

    override fun onCreate() {
        super.onCreate()
        // Creating the monitor starts it. Rules aren't re-applied here, so restarting the service,
        // for example when the app opens, doesn't undo a manual disconnect.
        get<NetworkMonitor>()
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int,
    ): Int {
        val notification = notificationManager.updateContentIntent(notificationIntent)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
        return START_STICKY
    }

    override fun onDestroy() {
        stopForeground(STOP_FOREGROUND_REMOVE)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}