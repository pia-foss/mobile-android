package com.kape.vpnconnect.data

import android.app.AlarmManager
import android.app.PendingIntent
import com.kape.connection.ConnectionPrefs
import com.kape.settings.SettingsPrefs
import com.kape.shareevents.domain.KpiDataSource
import com.kape.vpnconnect.domain.ConnectionDataSource
import com.kape.vpnmanager.data.models.ClientConfiguration
import com.kape.vpnmanager.presenters.VPNManagerAPI
import com.kape.vpnmanager.presenters.VPNManagerConnectionListener
import com.privateinternetaccess.account.AndroidAccountAPI
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import org.koin.core.component.KoinComponent

class ConnectionDataSourceImpl(
    private val connectionApi: VPNManagerAPI,
    private val accountApi: AndroidAccountAPI,
    private val connectionPrefs: ConnectionPrefs,
    private val alarmManager: AlarmManager,
    private val portForwardingIntent: PendingIntent,
    private val settingsPrefs: SettingsPrefs,
    private val kpiDataSource: KpiDataSource,
) : ConnectionDataSource, KoinComponent {

    override fun startConnection(
        clientConfiguration: ClientConfiguration,
        listener: VPNManagerConnectionListener,
    ): Flow<Boolean> =
        callbackFlow {
            connectionApi.addConnectionListener(listener) {}
            if (settingsPrefs.isHelpImprovePiaEnabled()) {
                kpiDataSource.start()
            } else {
                kpiDataSource.stop()
            }
            connectionApi.startConnection(clientConfiguration) {
                it.getOrNull()?.let { serverPeerInfo ->
                    connectionPrefs.setGateway(serverPeerInfo.gateway)
                    startPortForwarding()
                }
                trySend(it.isSuccess)
            }
            awaitClose { channel.close() }
        }

    override fun stopConnection(): Flow<Boolean> = callbackFlow {
        connectionApi.stopConnection {
            stopPortForwarding()
            trySend(it.isSuccess)
        }
        awaitClose { channel.close() }
    }

    override fun getVpnToken(): String {
        return accountApi.vpnToken() ?: ""
    }

    override fun startPortForwarding() {
        // TODO: handle how to pass status
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            0,
            AlarmManager.INTERVAL_FIFTEEN_MINUTES,
            portForwardingIntent,
        )
    }

    override fun stopPortForwarding() {
        // TODO: handle how to pass status
        connectionPrefs.clearGateway()
        connectionPrefs.clearPortBindingInfo()
        alarmManager.cancel(portForwardingIntent)
    }
}