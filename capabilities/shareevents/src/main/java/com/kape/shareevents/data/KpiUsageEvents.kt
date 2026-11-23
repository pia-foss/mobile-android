package com.kape.shareevents.data

import com.kape.contracts.ConnectionEvents
import com.kape.contracts.PurchaseEvents
import com.kape.contracts.UsageEventsControl
import com.kape.data.ConnectionStatus
import com.kape.data.kpi.KpiConnectionEvent
import com.kape.data.kpi.KpiConnectionSource
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
    private var lastConnectionStatus: ConnectionStatus = ConnectionStatus.DISCONNECTED

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
        status: ConnectionStatus,
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
    status: ConnectionStatus,
    previous: ConnectionStatus,
): KpiConnectionEvent? =
    when (status) {
        ConnectionStatus.CONNECTED -> KpiConnectionEvent.ConnectionEstablished
        ConnectionStatus.CONNECTING, ConnectionStatus.RECONNECTING -> KpiConnectionEvent.ConnectionAttempt
        // DISCONNECTING can be conflated away, so either state after CONNECTING is a cancellation.
        ConnectionStatus.DISCONNECTING, ConnectionStatus.DISCONNECTED ->
            if (previous == ConnectionStatus.CONNECTING) KpiConnectionEvent.ConnectionCancelled else null
        ConnectionStatus.PAUSED, ConnectionStatus.ERROR -> null
    }