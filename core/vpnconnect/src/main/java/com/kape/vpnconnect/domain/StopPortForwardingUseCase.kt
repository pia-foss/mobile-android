package com.kape.vpnconnect.domain

import com.kape.portforwarding.domain.PortForwardingUseCase

internal class StopPortForwardingUseCase(
    private val connectionDataSource: ConnectionDataSource,
    private val portForwardingUseCase: PortForwardingUseCase,
) {

    operator fun invoke() {
        connectionDataSource.stopPortForwarding()
        portForwardingUseCase.clearBindPort()
    }
}