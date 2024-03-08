package com.kape.vpn.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.kape.utils.AutomationManager
import com.kape.utils.NetworkConnectionListener
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class AutomationService : Service(), KoinComponent {

    private val automationManager: AutomationManager by inject()
    private val networkConnectionListener: NetworkConnectionListener by inject()

    override fun onCreate() {
        super.onCreate()
        startForeground(123, automationManager.notificationBuilder.build())
        networkConnectionListener.triggerUpdate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}