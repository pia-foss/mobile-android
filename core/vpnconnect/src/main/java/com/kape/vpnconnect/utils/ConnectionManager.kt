package com.kape.vpnconnect.utils

import com.kape.vpnmanager.presenters.VPNManagerConnectionListener
import com.kape.vpnmanager.presenters.VPNManagerConnectionStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ConnectionManager(
    private val connectionValues: Map<ConnectionStatus, String>,
) : VPNManagerConnectionListener {
    private val _connectionStatus =
        MutableStateFlow<ConnectionStatus>(ConnectionStatus.DISCONNECTED)
    val connectionStatus: StateFlow<ConnectionStatus> = _connectionStatus

    private val _serverName = MutableStateFlow("")
    val serverName: StateFlow<String> = _serverName

    private val _connectionStatusTitle = MutableStateFlow("")
    val connectionStatusTitle: StateFlow<String> = _connectionStatusTitle

    fun isConnected(): Boolean = connectionStatus.value == ConnectionStatus.CONNECTED

    fun setConnectedServerName(serverName: String) {
        _serverName.value = serverName
    }

    override fun handleConnectionStatusChange(status: VPNManagerConnectionStatus) {
        val currentStatus = when (status) {
            VPNManagerConnectionStatus.DISCONNECTED -> ConnectionStatus.DISCONNECTED
            VPNManagerConnectionStatus.CONNECTING -> ConnectionStatus.CONNECTING
            VPNManagerConnectionStatus.RECONNECTING -> ConnectionStatus.RECONNECTING
            VPNManagerConnectionStatus.CONNECTED -> ConnectionStatus.CONNECTED
        }

        _connectionStatus.value = currentStatus
        connectionValues[currentStatus]?.let {
            _connectionStatusTitle.value = String.format(it, serverName.value)
        }
    }
}