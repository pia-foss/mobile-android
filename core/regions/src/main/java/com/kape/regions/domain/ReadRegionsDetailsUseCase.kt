package com.kape.regions.domain

import com.kape.dip.DipPrefs
import com.kape.regions.data.RegionInputStream
import com.kape.regions.utils.adaptServers
import com.kape.utils.server.Server
import com.privateinternetaccess.regions.RegionsUtils

class ReadRegionsDetailsUseCase(
    private val regionInputStream: RegionInputStream,
    private val dipPrefs: DipPrefs,
) {

    companion object {
        private const val VPN_REGIONS_FILENAME = "vpn-regions.json"
        private const val SHADOWSOCKS_REGIONS_FILENAME = "shadowsocks-regions.json"
    }

    fun readVpnRegionsDetailsFromAssetsFolder(): List<Server> {
        val servers = regionInputStream.readAssetsFile(filename = VPN_REGIONS_FILENAME)
        val vpnRegionsResponse = RegionsUtils.parse(servers.split("\n\n").first())
        return adaptServers(vpnRegionsResponse, dipPrefs).values.toList()
    }
}