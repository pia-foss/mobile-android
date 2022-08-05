package com.kape.share_events.domain

import com.kape.share_events.models.KpiConnectionEvent
import com.kape.share_events.models.KpiConnectionSource
import kotlinx.coroutines.flow.Flow

interface KpiDataSource {

    fun start()

    fun stop()

    fun submit(connectionEvent: KpiConnectionEvent, connectionSource: KpiConnectionSource)

    fun flush()

    fun recentEvents(): Flow<List<String>>
}