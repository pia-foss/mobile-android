package com.kape.vpnregions.domain

import com.kape.data.RegionInputStream
import com.kape.data.RegionSerialization
import com.kape.dip.DipPrefs
import com.kape.utils.vpnserver.VpnServer
import com.kape.vpnregions.utils.adaptVpnServers

class ReadVpnRegionsDetailsUseCase(
    private val regionInputStream: RegionInputStream,
    private val regionSerialization: RegionSerialization,
    private val dipPrefs: DipPrefs,
) {

    companion object {
        private const val VPN_REGIONS_FILENAME = "vpn-regions.json"
    }

    fun readVpnRegionsDetailsFromAssetsFolder(): List<VpnServer> {
        val servers = regionInputStream.readAssetsFile(filename = VPN_REGIONS_FILENAME)
        val vpnRegionsResponse = regionSerialization.decodeVpnRegionsFromString(
            servers.split("\n\n").first(),
        )
        return adaptVpnServers(vpnRegionsResponse, dipPrefs).values.toList()
    }
}