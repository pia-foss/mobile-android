package com.kape.vpnconnect.domain

import com.kape.portforwarding.domain.PortForwardingUseCase

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

internal class StopPortForwardingUseCase(
    private val connectionDataSource: ConnectionDataSource,
    private val portForwardingUseCase: PortForwardingUseCase,
) {

    operator suspend fun invoke() {
        val context = currentCoroutineContext()
        context.ensureActive()
        connectionDataSource.stopPortForwarding()

        context.ensureActive()
        portForwardingUseCase.clearBindPort()
    }
}