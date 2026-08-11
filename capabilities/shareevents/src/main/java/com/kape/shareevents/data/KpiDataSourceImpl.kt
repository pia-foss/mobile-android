package com.kape.shareevents.data

import com.kape.contracts.KpiDataSource
import com.kape.data.kpi.KpiConnectionEvent
import com.kape.data.kpi.KpiConnectionSource
import com.privateinternetaccess.kpi.KPIAPI
import com.privateinternetaccess.kpi.KPIClientEvent
import kotlinx.coroutines.suspendCancellableCoroutine
import org.koin.core.annotation.Singleton
import kotlin.coroutines.resume

@Singleton(binds = [KpiDataSource::class])
class KpiDataSourceImpl(
    private val api: KPIAPI,
    private val eventGenerator: KpiEventGenerator,
) : KpiDataSource {
    override fun start() {
        api.start()
    }

    override fun stop() {
        api.stop { }
    }

    override suspend fun submitConnectionEvent(
        connectionEvent: KpiConnectionEvent,
        connectionSource: KpiConnectionSource,
    ) {
        api.submit(eventGenerator.getConnectionEvent(connectionEvent, connectionSource)) { }
    }

    override suspend fun submitEvent(event: KPIClientEvent) {
        api.submit(event) {}
    }

    override fun flush() {
        api.flush { }
    }

    override suspend fun recentEvents(): List<String> =
        suspendCancellableCoroutine { continuation ->
            api.recentEvents {
                continuation.resume(it)
            }
        }
}