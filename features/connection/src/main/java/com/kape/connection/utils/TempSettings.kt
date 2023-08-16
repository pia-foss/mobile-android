package com.kape.connection.utils

import com.kape.vpnmanager.presenters.VPNManagerProtocolTarget


// TODO: remove when settings module is implemented
data class TempSettings(
    val sessionName: String = "sessionName",
    val server: String = "138.199.59.242",
    val serverCommonName: String = "warsaw414",
    val protocol: VPNManagerProtocolTarget = VPNManagerProtocolTarget.OPENVPN,
    val mtu: Int = 1280,
    val port: Int = 8080,
    val dns: String = "10.0.0.243",
    val allowLocalNetworkAccess: Boolean = false,
    val openVpnCipher: String = "AES-128-GCM",
    val openVpnTransport: String = "udp"
)