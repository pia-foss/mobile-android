package com.kape.shareevents.data

import com.kape.contracts.ConnectionEvents
import com.kape.contracts.PurchaseEvents
import com.kape.contracts.UsageEventsControl
import com.kape.data.kpi.KpiConnectionEvent
import com.kape.data.kpi.KpiConnectionSource
import com.kape.data.kpi.KpiConnectionStatus
import com.privateinternetaccess.kpi.KPIAPI
import com.privateinternetaccess.kpi.KPIClientEvent
import kotlinx.coroutines.suspendCancellableCoroutine
import org.koin.core.annotation.Singleton
import kotlin.coroutines.resume

@Singleton(binds = [UsageEventsControl::class, ConnectionEvents::class, PurchaseEvents::class])
internal class KpiUsageEvents(
    private val api: KPIAPI,
    private val events: KpiEventGenerator,
) : UsageEventsControl,
    ConnectionEvents,
    PurchaseEvents {
    private var lastConnectionStatus: KpiConnectionStatus = KpiConnectionStatus.NotConnected

    override fun start() = api.start()

    override fun stop() = api.stop { }

    override fun flush() = api.flush { }

    override suspend fun recentEvents(): List<String> =
        suspendCancellableCoroutine { continuation ->
            api.recentEvents {
                continuation.resume(it)
            }
        }

    override suspend fun connectionStatusChanged(
        status: KpiConnectionStatus,
        isManual: Boolean,
    ) {
        if (status == lastConnectionStatus) return
        val source = if (isManual) KpiConnectionSource.Manual else KpiConnectionSource.Automatic
        connectionEventFor(status, lastConnectionStatus)?.let { submit(events.getConnectionEvent(it, source)) }
        lastConnectionStatus = status
    }

    override suspend fun purchaseStarted() = submit(events.getProcessingPurchaseEvent())

    override suspend fun purchaseSucceeded() = submit(events.getProcessingSuccess())

    override suspend fun purchaseFailed(reason: String) = submit(events.getProcessingFailure(reason))

    override suspend fun signupRetried(
        attempt: Int,
        error: String,
    ) = submit(events.getProcessingRetry(attempt, error))

    private fun submit(event: KPIClientEvent) = api.submit(event) { }
}

internal fun connectionEventFor(
    status: KpiConnectionStatus,
    previous: KpiConnectionStatus,
): KpiConnectionEvent? =
    when (status) {
        KpiConnectionStatus.Connected -> KpiConnectionEvent.ConnectionEstablished
        KpiConnectionStatus.Connecting, KpiConnectionStatus.Reconnecting -> KpiConnectionEvent.ConnectionAttempt
        KpiConnectionStatus.NotConnected -> if (previous == KpiConnectionStatus.Connecting) KpiConnectionEvent.ConnectionCancelled else null
        KpiConnectionStatus.Paused -> null
    }