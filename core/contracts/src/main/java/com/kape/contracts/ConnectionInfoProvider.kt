package com.kape.contracts

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import com.kape.data.VpnConnectionStatus
import com.kape.data.portforwarding.PortForwardingStatus
import kotlinx.coroutines.flow.StateFlow

interface ConnectionInfoProvider {
    val connectionInfoState: StateFlow<VpnConnectionStatus>
    var name: String
    var iso: String
    var isManual: Boolean
    val publicIp: StateFlow<String>
    val vpnIp: StateFlow<String>
    val portForwardingStatus: StateFlow<PortForwardingStatus>
    val port: StateFlow<String>

    fun isConnected(): Boolean

    fun isInConnectState(): Boolean

    fun isNotDisconnected(): Boolean

    fun updateInfo(
        name: String,
        iso: String,
        isManual: Boolean,
    )

    fun resetConnectionInfo()

    fun getTopBarConnectionColor(scheme: ColorScheme): Color
}