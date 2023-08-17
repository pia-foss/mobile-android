package com.kape.connection.utils

import androidx.compose.runtime.mutableStateOf
import com.kape.vpnmanager.presenters.VPNManagerConnectionListener
import com.kape.vpnmanager.presenters.VPNManagerConnectionStatus

class ConnectionListener : VPNManagerConnectionListener {

    private val connectionStatus = mutableStateOf(VPNManagerConnectionStatus.DISCONNECTED)

    override fun handleConnectionStatusChange(status: VPNManagerConnectionStatus) {
        connectionStatus.value = status
    }

    fun isConnected(): Boolean = connectionStatus.value == VPNManagerConnectionStatus.CONNECTED
}