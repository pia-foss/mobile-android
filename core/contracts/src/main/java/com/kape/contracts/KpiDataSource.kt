package com.kape.contracts

import com.kape.data.kpi.KpiConnectionEvent
import com.kape.data.kpi.KpiConnectionSource

interface KpiDataSource {
    fun start()

    fun stop()

    fun submit(
        connectionEvent: KpiConnectionEvent,
        connectionSource: KpiConnectionSource = KpiConnectionSource.Automatic,
    )

    fun flush()

    suspend fun recentEvents(): List<String>
}