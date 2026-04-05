package com.kape.shareevents.data

import android.os.SystemClock
import com.kape.contracts.ConfigInfo
import com.kape.contracts.KpiDataSource
import com.kape.data.kpi.KpiConnectionEvent
import com.kape.data.kpi.KpiConnectionSource
import com.kape.data.kpi.KpiEventPropertyKey
import com.kape.localprefs.prefs.SettingsPrefs
import com.kape.settings.data.VpnProtocols
import com.privateinternetaccess.kpi.KPIAPI
import com.privateinternetaccess.kpi.KPIClientEvent
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.datetime.Clock
import org.koin.core.annotation.Singleton
import kotlin.coroutines.resume

@Singleton(binds = [KpiDataSource::class])
class KpiDataSourceImpl(
    private val configInfo: ConfigInfo,
    private val api: KPIAPI,
    private val settingsPrefs: SettingsPrefs,
) : KpiDataSource {

    private var connectionInitiatedTime: Long = 0
    private var connectionEstablishedTime: Long = 0

    override fun start() {
        api.start()
    }

    override fun stop() {
        api.stop { }
    }

    override fun submit(
        connectionEvent: KpiConnectionEvent,
        connectionSource: KpiConnectionSource,
    ) {
        when (connectionEvent) {
            KpiConnectionEvent.ConnectionAttempt ->
                connectionInitiatedTime = SystemClock.elapsedRealtime()

            KpiConnectionEvent.ConnectionEstablished ->
                connectionEstablishedTime = SystemClock.elapsedRealtime()

            KpiConnectionEvent.ConnectionCancelled -> {
                connectionInitiatedTime = 0
                connectionEstablishedTime = 0
            }
        }

        val event = KPIClientEvent(
            eventName = connectionEvent.value,
            eventProperties = getEventProperties(connectionEvent, connectionSource),
            eventInstant = Clock.System.now(),
        )
        api.submit(event) { }
    }

    override fun flush() {
        api.flush { }
    }

    override suspend fun recentEvents(): List<String> = suspendCancellableCoroutine { continuation ->
        api.recentEvents {
            continuation.resume(it)
        }
    }

    private fun getEventProperties(
        connectionEvent: KpiConnectionEvent,
        connectionSource: KpiConnectionSource,
    ): Map<String, String> {
        val timeToConnect =
            (connectionEstablishedTime - connectionInitiatedTime).toFloat() / 1000
        val eventProperties = mutableMapOf<String, String>()
        eventProperties[KpiEventPropertyKey.ConnectionSource.value] = connectionSource.value
        eventProperties[KpiEventPropertyKey.UserAgent.value] = configInfo.userAgent
        eventProperties[KpiEventPropertyKey.VpnProtocol.value] =
            when (settingsPrefs.getSelectedProtocol()) {
                VpnProtocols.WireGuard -> VpnProtocols.WireGuard.name
                VpnProtocols.OpenVPN -> VpnProtocols.OpenVPN.name
            }
        if (connectionEvent == KpiConnectionEvent.ConnectionEstablished) {
            eventProperties[KpiEventPropertyKey.TimeToConnect.value] = timeToConnect.toString()
        }
        return eventProperties
    }
}