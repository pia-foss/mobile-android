package com.kape.data

import com.kape.data.portforwarding.PortForwardingStatus

data class VpnConnectionInfo(
    val name: String = "",
    val iso: String = "",
    val isManual: Boolean = false,
    val publicIp: String = NO_IP,
    val vpnIp: String = NO_IP,
    val portforwardingStatus: PortForwardingStatus = PortForwardingStatus.NoPortForwarding,
    val port: String = "",
)