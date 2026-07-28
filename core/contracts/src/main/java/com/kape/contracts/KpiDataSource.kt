package com.kape.contracts

import com.kape.data.kpi.KpiConnectionEvent
import com.kape.data.kpi.KpiConnectionSource
import com.privateinternetaccess.kpi.KPIClientEvent

interface KpiDataSource {
    fun start()

    fun stop()

    fun submitConnectionEvent(
        connectionEvent: KpiConnectionEvent,
        connectionSource: KpiConnectionSource = KpiConnectionSource.Automatic,
    )

    fun submitEvent(event: KPIClientEvent)

    fun flush()

    suspend fun recentEvents(): List<String>
}