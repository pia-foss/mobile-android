package com.kape.contracts

import com.kape.contracts.data.kpi.KpiConnectionEvent
import com.kape.contracts.data.kpi.KpiConnectionSource
import kotlinx.coroutines.flow.Flow

interface KpiDataSource {

    fun start()

    fun stop()

    fun submit(
        connectionEvent: KpiConnectionEvent,
        connectionSource: KpiConnectionSource = KpiConnectionSource.Automatic,
    )

    fun flush()

    fun recentEvents(): Flow<List<String>>
}