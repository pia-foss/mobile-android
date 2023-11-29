package com.kape.vpnregions.domain

import com.kape.utils.server.VpnServer
import com.kape.vpnregions.VpnRegionPrefs

class SetVpnRegionsUseCase(private val prefs: VpnRegionPrefs) {

    fun setVpnServers(servers: List<VpnServer>) = prefs.setVpnServers(vpnServers = servers)
}