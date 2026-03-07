package com.kape.vpn.service

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import com.kape.utils.AutomationManager
import com.kape.utils.NetworkConnectionListener
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named

class AutomationService : Service(), KoinComponent {
    private val automationManager: AutomationManager by inject()
    private val networkConnectionListener: NetworkConnectionListener by inject()
    private val automationPendingIntent: PendingIntent by inject(named("automation-pending-intent"))

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int,
    ): Int {
        val notification =
            automationManager.notificationBuilder.setContentIntent(automationPendingIntent).build()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                123,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION,
            )
        } else {
            startForeground(123, notification)
        }
        networkConnectionListener.triggerUpdate()
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}