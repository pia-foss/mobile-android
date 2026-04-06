package com.kape.vpnconnect.domain

import com.kape.contracts.ConnectionInfoProvider

class StopConnectionUseCase(
    private val connectionInfoProvider: ConnectionInfoProvider,
    private val connectionSource: ConnectionDataSource,
    private val stopShadowsocksUseCase: StopShadowsocksUseCase,
    private val stopPortForwardingUseCase: StopPortForwardingUseCase,
) {

    operator suspend fun invoke(): Boolean {
        if (connectionInfoProvider.isInConnectState()) {
            val result = connectionSource.stopConnection()
            connectionInfoProvider.resetConnectionInfo()
            stopShadowsocksUseCase()
            stopPortForwardingUseCase()
            // TODO: implement what's commented out
//        clientStateDataSource.resetVpnIp()
//        vpnIp.value = connectionPrefs.getVpnIp()
//        stopPortForwarding()
            return result
        } else {
            return false
        }

    }

}