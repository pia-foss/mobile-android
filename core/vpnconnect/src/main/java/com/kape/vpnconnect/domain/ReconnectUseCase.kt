package com.kape.vpnconnect.domain

import com.kape.utils.vpnserver.VpnServer
import com.kape.vpnconnect.utils.ConnectionInfoProvider

class ReconnectUseCase(
    private val startConnectionUseCase: StartConnectionUseCase,
    private val stopConnectionUseCase: StopConnectionUseCase,
    private val connectionInfoProvider: ConnectionInfoProvider,
) {
    operator suspend fun invoke(server: VpnServer): Boolean {
        println("--- reconnect to ${server.name}")
        if (connectionInfoProvider.isInConnectState()) {
            stopConnectionUseCase()
        }
        return startConnectionUseCase(server, true)
    }
}