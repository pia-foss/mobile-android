package com.kape.shareevents.domain

import com.kape.shareevents.data.models.KpiConnectionEvent
import com.kape.shareevents.data.models.KpiConnectionSource
import com.kape.shareevents.data.models.KpiConnectionStatus

class SubmitKpiEventUseCase(
    private val api: KpiDataSource,
) {
    private var kpiConnectionStatus: KpiConnectionStatus = KpiConnectionStatus.NotConnected

    fun submitConnectionEvent(status: KpiConnectionStatus, isManualConnection: Boolean) {
        val connectionSource =
            if (isManualConnection) KpiConnectionSource.Manual else KpiConnectionSource.Automatic
        if (kpiConnectionStatus == status) {
            return
        }
        when (status) {
            KpiConnectionStatus.Connected -> {
                api.submit(KpiConnectionEvent.ConnectionEstablished, connectionSource)
            }

            KpiConnectionStatus.Connecting -> {
                api.submit(KpiConnectionEvent.ConnectionAttempt, connectionSource)
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