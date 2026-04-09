package com.kape.vpnconnect.domain

import com.kape.contracts.ConnectionInfoProvider
import com.kape.data.vpnserver.VpnServer

internal class ReconnectUseCase(
    private val startConnectionUseCase: StartConnectionUseCase,
    private val stopConnectionUseCase: StopConnectionUseCase,
    private val connectionInfoProvider: ConnectionInfoProvider,
) {
    operator suspend fun invoke(server: VpnServer): Boolean {
        if (connectionInfoProvider.isInConnectState()) {
            stopConnectionUseCase()
        }
        return startConnectionUseCase(server, true)
    }
}