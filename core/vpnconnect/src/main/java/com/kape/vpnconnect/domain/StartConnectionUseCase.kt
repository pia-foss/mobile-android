package com.kape.vpnconnect.domain

import com.kape.contracts.ConnectionConfigurationUseCase
import com.kape.contracts.ConnectionInfoProvider
import com.kape.contracts.ConnectionStatusProvider
import com.kape.data.vpnserver.VpnServer
import com.kape.localprefs.prefs.ConnectionPrefs
import com.kape.vpnconnect.utils.ConnectionInfoProviderImpl
import com.kape.vpnconnect.utils.ConnectionStatusProviderImpl
import com.kape.vpnmanager.presenters.VPNManagerConnectionListener

class StartConnectionUseCase(
    private val connectionSource: ConnectionDataSource,
    private val connectionInfoProvider: ConnectionInfoProvider,
    private val connectionPrefs: ConnectionPrefs,
    private val startShadowsocksUseCase: StartShadowsocksUseCase,
    private val stopShadowsocksUseCase: StopShadowsocksUseCase,
    private val connectionConfigurationUseCase: ConnectionConfigurationUseCase,
    private val connectionStatusProvider: ConnectionStatusProvider,
    private val startPortForwardingUseCase: StartPortForwardingUseCase
) {
    operator suspend fun invoke(server: VpnServer, isManualConnection: Boolean): Boolean {
        if (!connectionInfoProvider.isInConnectState()) {
            connectionInfoProvider.updateInfo(server.name, server.iso, isManualConnection)
            connectionPrefs.setSelectedVpnServer(server)
            connectionPrefs.addToQuickConnect(
                server.key,
                server.isDedicatedIp,
            )
            val succeeded = startShadowsocksUseCase()
            if (!succeeded) return false
            val connected = startVpnConnection(server)
            if (!connected) stopShadowsocksUseCase()
            return connected
        } else {
            return false
        }
    }

    private suspend fun startVpnConnection(server: VpnServer): Boolean {
        val connected = connectionSource.startConnection(
            connectionConfigurationUseCase.generateConnectionConfiguration(server = server),
            connectionStatusProvider as VPNManagerConnectionListener,
        )
        startPortForwardingUseCase()
        return connected
    }
}