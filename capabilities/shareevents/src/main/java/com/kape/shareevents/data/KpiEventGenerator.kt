package com.kape.shareevents.data

import android.os.SystemClock
import com.kape.contracts.AppInfo
import com.kape.contracts.ConfigInfo
import com.kape.data.kpi.KpiConnectionEvent
import com.kape.data.kpi.KpiConnectionSource
import com.kape.data.kpi.KpiEventPropertyKey
import com.kape.localprefs.prefs.SettingsPrefs
import com.kape.settings.data.VpnProtocols
import com.privateinternetaccess.kpi.KPIClientEvent
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Clock

private const val IAP_PROCESSING_PURCHASE = "iap_processing_purchase"
private const val IAP_PROCESSING_SUCCESS = "iap_processing_success"
private const val IAP_PROCESSING_FAILURE = "iap_processing_failure"
private const val IAP_PROCESSING_RETRY = "iap_processing_retry"

private const val ORIGIN = "signup"
private const val ENVIRONMENT = "production"

class KpiEventGenerator(
    private val appInfo: AppInfo,
    private val configInfo: ConfigInfo,
    private val settingsPrefs: SettingsPrefs,
) {
    private var connectionInitiatedTime: Long = 0
    private var connectionEstablishedTime: Long = 0

    private fun getDefaultEventProperties(): Map<String, String> =
        mapOf(
            KpiEventPropertyKey.Platform.value to "Android",
            KpiEventPropertyKey.Version.value to appInfo.versionName,
        )

    fun getProcessingPurchaseEvent(): KPIClientEvent =
        KPIClientEvent(
            eventName = IAP_PROCESSING_PURCHASE,
            eventProperties =
                getDefaultEventProperties() +
                    mapOf(
                        KpiEventPropertyKey.Origin.value to ORIGIN,
                        KpiEventPropertyKey.Environment.value to ENVIRONMENT,
                    ),
            eventInstant = Clock.System.now(),
        )

    fun getProcessingSuccess(): KPIClientEvent =
        KPIClientEvent(
            eventName = IAP_PROCESSING_SUCCESS,
            eventProperties =
                getDefaultEventProperties() +
                    mapOf(
                        KpiEventPropertyKey.Origin.value to ORIGIN,
                        KpiEventPropertyKey.Environment.value to ENVIRONMENT,
                    ),
            eventInstant = Clock.System.now(),
        )

    fun getProcessingFailure(error: String): KPIClientEvent =
        KPIClientEvent(
            eventName = IAP_PROCESSING_FAILURE,
            eventProperties =
                getDefaultEventProperties() +
                    mapOf(
                        KpiEventPropertyKey.Origin.value to ORIGIN,
                        KpiEventPropertyKey.Environment.value to ENVIRONMENT,
                        KpiEventPropertyKey.Error.value to error,
                    ),
            eventInstant = Clock.System.now(),
        )

    fun getProcessingRetry(
        retryCount: Int,
        error: String,
    ): KPIClientEvent =
        KPIClientEvent(
            eventName = IAP_PROCESSING_RETRY,
            eventProperties =
                getDefaultEventProperties() +
                    mapOf(
                        KpiEventPropertyKey.Origin.value to ORIGIN,
                        KpiEventPropertyKey.Environment.value to ENVIRONMENT,
                        KpiEventPropertyKey.Error.value to error,
                        KpiEventPropertyKey.RetryCount.value to "$retryCount",
                    ),
            eventInstant = Clock.System.now(),
        )

    suspend fun getConnectionEvent(
        connectionEvent: KpiConnectionEvent,
        connectionSource: KpiConnectionSource,
    ): KPIClientEvent {
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

        val event =
            KPIClientEvent(
                eventName = connectionEvent.value,
                eventProperties =
                    getDefaultEventProperties() +
                        getConnectionEventProperties(
                            connectionEvent,
                            connectionSource,
                        ),
                eventInstant = Clock.System.now(),
            )
        return event
    }

    private suspend fun getConnectionEventProperties(
        connectionEvent: KpiConnectionEvent,
        connectionSource: KpiConnectionSource,
    ): Map<String, String> {
        val timeToConnect =
            (connectionEstablishedTime - connectionInitiatedTime).toFloat() / 1000
        val eventProperties = mutableMapOf<String, String>()
        eventProperties[KpiEventPropertyKey.ConnectionSource.value] = connectionSource.value
        eventProperties[KpiEventPropertyKey.UserAgent.value] = configInfo.userAgent
        eventProperties[KpiEventPropertyKey.VpnProtocol.value] =
            when (settingsPrefs.selectedProtocol.first()) {
                VpnProtocols.WireGuard -> VpnProtocols.WireGuard.name
                VpnProtocols.OpenVPN -> VpnProtocols.OpenVPN.name
                VpnProtocols.Automatic -> VpnProtocols.Automatic.name
            }
        if (connectionEvent == KpiConnectionEvent.ConnectionEstablished) {
            eventProperties[KpiEventPropertyKey.TimeToConnect.value] = timeToConnect.toString()
        }
        return eventProperties
    }
}