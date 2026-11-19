package com.kape.contracts

import com.kape.data.ConnectionStatus
import com.kape.data.portforwarding.PortForwardingStatus
import kotlinx.coroutines.flow.StateFlow

interface ConnectionInfoProvider {
    val status: StateFlow<ConnectionStatus>
    val title: StateFlow<String>

    val name: String
    val iso: String
    val isManual: Boolean

    val publicIp: StateFlow<String>
    val vpnIp: StateFlow<String>

    val portForwardingStatus: StateFlow<PortForwardingStatus>
    val port: StateFlow<String>

    fun isConnected(): Boolean

    fun isInConnectState(): Boolean

    fun shouldAllowDisconnect(): Boolean

    fun requestClientIp()
}