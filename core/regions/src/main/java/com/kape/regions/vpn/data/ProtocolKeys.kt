package com.kape.regions.vpn.data

import com.kape.data.vpnserver.VpnServer.ServerGroup
import com.privateinternetaccess.regions.RegionsProtocol

fun ServerGroup.openVpnApiKey(): String? =
    when (this) {
        ServerGroup.OPENVPN_UDP -> RegionsProtocol.OPENVPN_UDP.protocol
        ServerGroup.OPENVPN_TCP -> RegionsProtocol.OPENVPN_TCP.protocol
        else -> null
    }