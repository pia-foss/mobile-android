package com.kape.utils

import android.util.Log
import com.kape.vpnmanager.presenters.VPNManagerConnectionListener
import com.kape.vpnmanager.presenters.VPNManagerConnectionStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow

class ConnectionListener : VPNManagerConnectionListener {

    private val _serverName = MutableStateFlow("")
    val serverName: StateFlow<String> = _serverName

    private val _connectionStatus =
        MutableStateFlow<ConnectionStatus>(ConnectionStatus.DISCONNECTED)
    val connectionStatus: StateFlow<ConnectionStatus> = _connectionStatus

    override
    fun handleConnectionStatusChange(status: VPNManagerConnectionStatus) {
        val currentStatus = when (status) {
            VPNManagerConnectionStatus.DISCONNECTED -> ConnectionStatus.DISCONNECTED
            VPNManagerConnectionStatus.CONNECTING -> ConnectionStatus.CONNECTING
            VPNManagerConnectionStatus.RECONNECTING -> ConnectionStatus.RECONNECTING
            VPNManagerConnectionStatus.CONNECTED -> ConnectionStatus.CONNECTED
        }
        _connectionStatus.value = currentStatus
        if (currentStatus is ConnectionStatus.DISCONNECTED) {
            _serverName.value = ""
        }
        Log.e("aaa", "status: $status")
    }

    fun setCurrentServerName(name: String) {
        _serverName.value = name
        Log.e("aaa", "server: $name")
    }

    fun isConnected(): Boolean = connectionStatus.value == ConnectionStatus.CONNECTED

    sealed class ConnectionStatus {
        data object DISCONNECTED : ConnectionStatus()
        data object CONNECTED : ConnectionStatus()
        data object CONNECTING : ConnectionStatus()
        data object RECONNECTING : ConnectionStatus()
    }
}