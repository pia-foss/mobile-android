package com.kape.connection.domain.usecase

import com.kape.contracts.ConnectionInfoProvider
import com.kape.contracts.ConnectionManager
import com.kape.contracts.SnoozeStatus
import com.kape.contracts.di.Qualifiers
import com.kape.data.vpnserver.VpnServer
import com.kape.localprefs.api.ConnectionStore
import com.kape.localprefs.api.SettingsStore
import com.kape.localprefs.api.VpnRegionStore
import com.kape.regions.vpn.domain.usecase.GetOptimalVpnServerUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Named
import org.koin.core.annotation.Singleton

// These run in the app scope, so leaving the connection screen never cancels a connection
// half-way, and disconnecting after the connection stops by itself still happens.

@Factory
class ConnectUseCase(
    private val connectionManager: ConnectionManager,
    private val snooze: SnoozeStatus,
    private val getOptimalVpnServer: GetOptimalVpnServerUseCase,
    @Named(Qualifiers.APP_SCOPE) private val appScope: CoroutineScope,
) {
    operator fun invoke(
        server: VpnServer?,
        isManual: Boolean = true,
        onProtocolNotAvailable: () -> Unit,
    ) = appScope.launch {
        val target = server?.takeIf { it.endpoints.isNotEmpty() } ?: getOptimalVpnServer()
        snooze.cancelSnooze()
        if (connectionManager.isConnectionInProgress()) {
            connectionManager.disconnect()
        }
        connectionManager.connect(
            server = target,
            isManual = isManual,
            stopCallback = { appScope.launch { connectionManager.disconnect() } },
            showDialog = onProtocolNotAvailable,
        )
    }
}

@Factory
class DisconnectUseCase(
    private val connectionManager: ConnectionManager,
    private val connectionPrefs: ConnectionStore,
    private val settingsPrefs: SettingsStore,
    @Named(Qualifiers.APP_SCOPE) private val appScope: CoroutineScope,
) {
    operator fun invoke() =
        appScope.launch {
            if (settingsPrefs.isAutomationEnabledNow()) {
                connectionPrefs.setDisconnectedByUser(true)
            }
            connectionManager.disconnect()
        }
}

@Factory
class QuickConnectUseCase(
    private val connectionManager: ConnectionManager,
    private val vpnRegionPrefs: VpnRegionStore,
    private val snooze: SnoozeStatus,
    @Named(Qualifiers.APP_SCOPE) private val appScope: CoroutineScope,
) {
    operator fun invoke(server: VpnServer) =
        appScope.launch {
            vpnRegionPrefs.selectVpnServer(server)
            snooze.cancelSnooze()
            connectionManager.reconnect(server, stopCallback = { appScope.launch { connectionManager.disconnect() } })
        }
}

// A singleton, not @Factory: handled must survive the connection screen being recreated, or
// every recreation would connect again.
@Singleton
class ConnectOnLaunchUseCase(
    private val settingsPrefs: SettingsStore,
    private val connectionPrefs: ConnectionStore,
    private val connectionInfo: ConnectionInfoProvider,
    private val connect: ConnectUseCase,
) {
    private var handled = false

    suspend operator fun invoke(onProtocolNotAvailable: () -> Unit) {
        if (handled) return
        handled = true
        // Read the stored end time, as the snooze itself may not be restored yet after the app was killed.
        val isSnoozed = connectionPrefs.getLastSnoozeEndTimeNow() > System.currentTimeMillis()
        if (settingsPrefs.isConnectOnLaunchEnabledNow() && !isSnoozed && !connectionInfo.isConnected()) {
            connect(connectionPrefs.getSelectedVpnServerNow(), isManual = false, onProtocolNotAvailable).join()
        }
    }
}