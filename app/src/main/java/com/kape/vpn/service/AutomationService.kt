package com.kape.vpn.service

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import com.kape.utils.AutomationManager
import com.kape.utils.NetworkConnectionListener
import org.koin.core.annotation.Singleton
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject
import org.koin.core.qualifier.named

@Singleton
class AutomationService :
    Service(),
    KoinComponent {
    private val automationManager: AutomationManager by inject()

    // Resolved eagerly so the listener registers its network callback as soon as the service is
    // (re)created. Rules are re-applied for the current network by OnRulesChangedReceiver, not
    // here, so restarting the service (e.g. on app launch) doesn't override a manual disconnect.
    @Suppress("unused")
    private val networkConnectionListener: NetworkConnectionListener = get()
    private val automationPendingIntent: PendingIntent by inject(named("automation-pending-intent"))

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int,
    ): Int {
        val notification = automationManager.vpnNotificationManager.updateContentIntent(automationPendingIntent)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                123,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE,
            )
        } else {
            startForeground(123, notification)
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}