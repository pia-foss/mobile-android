package com.kape.utils

import com.kape.vpnmanager.presenters.VPNManagerConnectionListener
import com.kape.vpnmanager.presenters.VPNManagerConnectionStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ConnectionListener(private val values: Map<ConnectionStatus, String>) :
    VPNManagerConnectionListener {

    private val _serverName = MutableStateFlow("")
    private val serverName: StateFlow<String> = _serverName

    private val _connectionStatus =
        MutableStateFlow<Pair<ConnectionStatus, String?>>(
            Pair(
                ConnectionStatus.DISCONNECTED,
                values[ConnectionStatus.DISCONNECTED]
            )
        )
    val connectionStatus: StateFlow<Pair<ConnectionStatus, String?>> = _connectionStatus

    override
    fun handleConnectionStatusChange(status: VPNManagerConnectionStatus) {
        val currentStatus = when (status) {
            VPNManagerConnectionStatus.DISCONNECTED -> ConnectionStatus.DISCONNECTED
            VPNManagerConnectionStatus.CONNECTING -> ConnectionStatus.CONNECTING
            VPNManagerConnectionStatus.RECONNECTING -> ConnectionStatus.RECONNECTING
            VPNManagerConnectionStatus.CONNECTED -> ConnectionStatus.CONNECTED
        }
        if (currentStatus is ConnectionStatus.DISCONNECTED) {
            _serverName.value = ""
        }

        values[currentStatus]?.let {
            _connectionStatus.value =
                Pair(currentStatus, String.format(it, serverName.value))
        }
    }

    fun setCurrentServerName(name: String) {
        _serverName.value = name
    }

    fun isConnected(): Boolean = connectionStatus.value.first == ConnectionStatus.CONNECTED

    sealed class ConnectionStatus {
        data object DISCONNECTED : ConnectionStatus()
        data object CONNECTED : ConnectionStatus()
        data object CONNECTING : ConnectionStatus()
        data object RECONNECTING : ConnectionStatus()
    }
}