package com.kape.vpnregionselection.domain

import com.kape.data.vpnserver.VpnServer
import com.kape.data.vpnserver.VpnServer.ServerGroup
import com.kape.settings.data.Transport
import com.kape.settings.data.VpnProtocols

internal fun serverGroupFor(
    protocol: VpnProtocols,
    transport: Transport,
): ServerGroup? =
    when (protocol) {
        VpnProtocols.WireGuard -> ServerGroup.WIREGUARD
        VpnProtocols.OpenVPN -> if (transport == Transport.TCP) ServerGroup.OPENVPN_TCP else ServerGroup.OPENVPN_UDP
        VpnProtocols.Automatic -> null
    }

internal fun VpnServer.isListed(
    serverGroup: ServerGroup?,
    showGeoLocated: Boolean,
): Boolean = (showGeoLocated || !isGeo) && (serverGroup == null || !endpoints[serverGroup].isNullOrEmpty())