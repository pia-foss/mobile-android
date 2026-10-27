package com.kape.location.mocklocation

import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class MockLocationService :
    Service(),
    KoinComponent {
    private val notificationProvider: MockLocationNotificationProvider by inject()
    private val fusedLocationMocker: FusedLocationMocker by inject()

    // The burst pushes every 50ms, so keep those binder calls off the main thread.
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var mock: MockLocationProvider? = null

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int,
    ): Int {
        val lat = intent?.getDoubleExtra(EXTRA_LATITUDE, 0.0) ?: 0.0
        val lng = intent?.getDoubleExtra(EXTRA_LONGITUDE, 0.0) ?: 0.0

        val notification = notificationProvider.build()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                MockLocationNotificationProvider.NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION,
            )
        } else {
            startForeground(MockLocationNotificationProvider.NOTIFICATION_ID, notification)
        }

        val provider = mock ?: MockLocationProvider(this, fusedLocationMocker, scope).also { mock = it }
        if (!provider.push(lat, lng)) {
            // PIA is no longer the selected mock location app in Developer options.
            stopSelf()
            return START_NOT_STICKY
        }
        return START_STICKY
    }

    override fun onDestroy() {
        scope.cancel()
        mock?.stop()
        stopForeground(STOP_FOREGROUND_REMOVE)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val EXTRA_LATITUDE = "latitude"
        const val EXTRA_LONGITUDE = "longitude"
    }
}