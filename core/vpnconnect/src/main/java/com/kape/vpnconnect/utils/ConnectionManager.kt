package com.kape.vpnconnect.utils

import com.kape.vpnconnect.domain.ConnectionUseCase
import com.kape.vpnmanager.presenters.VPNManagerConnectionListener
import com.kape.vpnmanager.presenters.VPNManagerConnectionStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ConnectionManager(
    private val connectionUseCase: ConnectionUseCase,
    private val connectionValues: Map<ConnectionStatus, String>
) : VPNManagerConnectionListener {
    private val _connectionStatus =
        MutableStateFlow<ConnectionStatus>(ConnectionStatus.DISCONNECTED)
    val connectionStatus: StateFlow<ConnectionStatus> = _connectionStatus

    private val _connectionStatusTitle = MutableStateFlow("")
    val connectionStatusTitle: StateFlow<String> = _connectionStatusTitle

    fun isConnected(): Boolean = connectionStatus.value == ConnectionStatus.CONNECTED

    override fun handleConnectionStatusChange(status: VPNManagerConnectionStatus) {
        val currentStatus = when (status) {
            VPNManagerConnectionStatus.DISCONNECTED -> ConnectionStatus.DISCONNECTED
            VPNManagerConnectionStatus.CONNECTING -> ConnectionStatus.CONNECTING
            VPNManagerConnectionStatus.RECONNECTING -> ConnectionStatus.RECONNECTING
            VPNManagerConnectionStatus.CONNECTED -> ConnectionStatus.CONNECTED
        }

        _connectionStatus.value = currentStatus
        connectionValues[currentStatus]?.let {
            _connectionStatusTitle.value = String.format(it, connectionUseCase.serverName.value)
        }
    }
}