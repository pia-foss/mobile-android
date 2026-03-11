package com.kape.vpnconnect.domain

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.kape.portforwarding.data.model.PortForwardingStatus
import com.kape.utils.vpnserver.VpnServer
import com.kape.vpnconnect.utils.ConnectionStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface ConnectionUseCase {
    val portForwardingStatus: MutableState<PortForwardingStatus>
    val port: MutableState<String?>
    val clientIp: MutableState<String>
    val vpnIp: MutableState<String>
    fun startConnection(server: VpnServer, isManualConnection: Boolean): Flow<Boolean>
    fun stopConnection(): Flow<Boolean>
    fun reconnect(server: VpnServer): Flow<Boolean>
    fun isConnected(): Boolean
    fun isConnecting(): Boolean
    fun isNotDisconnected(): Boolean
    fun getClientStatus(status: ConnectionStatus): Flow<Boolean>
    fun getConnectionStatus(): StateFlow<ConnectionStatus>
    fun resetVpnIp()
}