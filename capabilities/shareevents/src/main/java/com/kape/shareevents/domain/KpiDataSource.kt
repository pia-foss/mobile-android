package com.kape.shareevents.domain

import com.kape.shareevents.data.models.KpiConnectionEvent
import com.kape.shareevents.data.models.KpiConnectionSource
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