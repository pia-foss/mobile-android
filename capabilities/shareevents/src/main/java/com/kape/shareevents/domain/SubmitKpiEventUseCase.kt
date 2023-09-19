package com.kape.shareevents.domain

import android.os.SystemClock
import com.kape.shareevents.data.models.KpiConnectionEvent
import com.kape.shareevents.data.models.KpiConnectionSource
import com.kape.shareevents.data.models.KpiConnectionStatus

class SubmitKpiEventUseCase(
    private val api: KpiDataSource,
) {
    private var connectionInitiatedTime: Long = 0
    private var connectionEstablishedTime: Long = 0
    private var kpiConnectionStatus: KpiConnectionStatus = KpiConnectionStatus.NotConnected

    fun submitConnectionEvent(status: KpiConnectionStatus, isManualConnection: Boolean) {
        val connectionSource =
            if (isManualConnection) KpiConnectionSource.Manual else KpiConnectionSource.Automatic
        if (kpiConnectionStatus == status) {
            return
        }
        when (status) {
            KpiConnectionStatus.Connected -> {
                connectionEstablishedTime = SystemClock.elapsedRealtime()
                api.submit(KpiConnectionEvent.ConnectionEstablished, connectionSource)
            }

            KpiConnectionStatus.Connecting -> {
                connectionInitiatedTime = SystemClock.elapsedRealtime()
                api.submit(
                    KpiConnectionEvent.ConnectionAttempt, connectionSource,
                )
            }

            KpiConnectionStatus.NotConnected -> {
                if (kpiConnectionStatus == KpiConnectionStatus.Connecting) {
                    api.submit(KpiConnectionEvent.ConnectionCancelled, connectionSource)
                }
            }

            KpiConnectionStatus.Reconnecting -> {
                // no-op
            }
        }
        kpiConnectionStatus = status
    }
}