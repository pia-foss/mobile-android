package com.kape.shareevents.data

import com.kape.shareevents.KpiPrefs
import com.kape.shareevents.data.models.KpiConnectionEvent
import com.kape.shareevents.data.models.KpiConnectionSource
import com.kape.shareevents.data.models.KpiEventPropertyKey
import com.kape.shareevents.domain.KpiDataSource
import com.privateinternetaccess.kpi.KPIAPI
import com.privateinternetaccess.kpi.KPIClientEvent
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.datetime.Clock
import org.koin.core.component.KoinComponent

class KpiDataSourceImpl(
    private val prefs: KpiPrefs,
    private val userAgent: String,
    private val api: KPIAPI,
) : KpiDataSource, KoinComponent {

    private var connectionInitiatedTime: Long = 0
    private var connectionEstablishedTime: Long = 0

    override fun start() {
        api.start()
    }

    override fun stop() {
        api.stop {
            // TODO: handle error?
        }
    }

    override fun submit(
        connectionEvent: KpiConnectionEvent,
        connectionSource: KpiConnectionSource,
    ) {
        val event =
            KPIClientEvent(
                eventName = connectionEvent.value,
                eventProperties = getEventProperties(connectionEvent, connectionSource),
                eventInstant = Clock.System.now(),
            )
        api.submit(event) {
            // TODO: handle error?
        }
    }

    override fun flush() {
        api.flush {
            // TODO: handle error?
        }
    }

    override fun recentEvents(): Flow<List<String>> = callbackFlow {
        api.recentEvents {
            trySend(it)
        }
        awaitClose { channel.close() }
    }

    private fun getEventProperties(
        connectionEvent: KpiConnectionEvent,
        connectionSource: KpiConnectionSource,
    ): Map<String, String> {
        val timeToConnect =
            (connectionEstablishedTime - connectionInitiatedTime).toFloat() / 1000
        val eventProperties = mutableMapOf<String, String>()
        eventProperties[KpiEventPropertyKey.ConnectionSource.value] = connectionSource.value
        eventProperties[KpiEventPropertyKey.UserAgent.value] = userAgent
        eventProperties[KpiEventPropertyKey.VpnProtocol.value] = prefs.getActiveProtocol()
        if (connectionEvent == KpiConnectionEvent.ConnectionEstablished) {
            eventProperties[KpiEventPropertyKey.TimeToConnect.value] = timeToConnect.toString()
        }
        return eventProperties
    }
}