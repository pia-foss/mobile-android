package com.kape.vpnconnect.domain

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.kape.portforwarding.data.model.PortForwardingStatus
import com.kape.utils.vpnserver.VpnServer
import com.kape.vpnconnect.utils.ConnectionStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

interface ConnectionUseCase {
    val portForwardingStatus: MutableStateFlow<PortForwardingStatus>
    val port: MutableStateFlow<String>
    val clientIp: MutableStateFlow<String>
    val vpnIp: MutableStateFlow<String>
    suspend fun startConnection(server: VpnServer, isManualConnection: Boolean): Boolean
    suspend fun stopConnection(): Boolean
    suspend fun reconnect(server: VpnServer): Boolean
    fun isConnected(): Boolean
    fun isConnecting(): Boolean
    fun isNotDisconnected(): Boolean
    suspend fun getClientStatus(status: ConnectionStatus): ConnectionStatus
    fun getConnectionStatus(): StateFlow<ConnectionStatus>
    fun resetVpnIp(): StateFlow<String>
}