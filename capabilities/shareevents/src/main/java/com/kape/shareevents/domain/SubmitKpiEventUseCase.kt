package com.kape.shareevents.domain

import com.kape.contracts.KpiDataSource
import com.kape.data.kpi.KpiConnectionEvent
import com.kape.data.kpi.KpiConnectionSource
import com.kape.data.kpi.KpiConnectionStatus
import com.privateinternetaccess.kpi.KPIClientEvent
import org.koin.core.annotation.Singleton

@Singleton
class SubmitKpiEventUseCase(
    private val api: KpiDataSource,
) {
    private var kpiConnectionStatus: KpiConnectionStatus = KpiConnectionStatus.NotConnected

    fun submitConnectionEvent(
        status: KpiConnectionStatus,
        isManualConnection: Boolean,
    ) {
        val connectionSource =
            if (isManualConnection) KpiConnectionSource.Manual else KpiConnectionSource.Automatic
        if (kpiConnectionStatus == status) {
            return
        }
        when (status) {
            KpiConnectionStatus.Connected -> {
                api.submitConnectionEvent(KpiConnectionEvent.ConnectionEstablished, connectionSource)
            }

            KpiConnectionStatus.Connecting -> {
                api.submitConnectionEvent(KpiConnectionEvent.ConnectionAttempt, connectionSource)
            }

            KpiConnectionStatus.NotConnected -> {
                if (kpiConnectionStatus == KpiConnectionStatus.Connecting) {
                    api.submitConnectionEvent(KpiConnectionEvent.ConnectionCancelled, connectionSource)
                }
            }

            KpiConnectionStatus.Reconnecting -> {
                // no-op
            }
        }
        kpiConnectionStatus = status
    }

    fun submitEvent(event: KPIClientEvent) {
        api.submitEvent(event)
    }
}