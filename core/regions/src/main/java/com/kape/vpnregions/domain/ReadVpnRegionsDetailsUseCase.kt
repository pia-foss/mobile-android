package com.kape.vpnregions.domain

import com.kape.dip.DipPrefs
import com.kape.utils.server.VpnServer
import com.kape.vpnregions.data.VpnRegionInputStream
import com.kape.vpnregions.utils.adaptServers
import com.privateinternetaccess.regions.RegionsUtils

class ReadVpnRegionsDetailsUseCase(
    private val regionInputStream: VpnRegionInputStream,
    private val dipPrefs: DipPrefs,
) {

    companion object {
        private const val VPN_REGIONS_FILENAME = "vpn-regions.json"
        private const val SHADOWSOCKS_REGIONS_FILENAME = "shadowsocks-regions.json"
    }

    fun readVpnRegionsDetailsFromAssetsFolder(): List<VpnServer> {
        val servers = regionInputStream.readAssetsFile(filename = VPN_REGIONS_FILENAME)
        val vpnRegionsResponse = RegionsUtils.parse(servers.split("\n\n").first())
        return adaptServers(vpnRegionsResponse, dipPrefs).values.toList()
    }
}