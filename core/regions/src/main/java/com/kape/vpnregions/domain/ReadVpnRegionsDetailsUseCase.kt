package com.kape.vpnregions.domain

import com.kape.data.RegionInputStream
import com.kape.data.RegionSerialization
import com.kape.data.vpnserver.VpnServer
import com.kape.data.vpnserver.VpnServerInfo
import com.kape.vpnregions.utils.adaptServersInfo
import com.kape.vpnregions.utils.adaptVpnServers
import com.privateinternetaccess.regions.model.VpnRegionsResponse
import org.koin.core.annotation.Singleton

@Singleton
class ReadVpnRegionsDetailsUseCase(
    private val regionInputStream: RegionInputStream,
    private val regionSerialization: RegionSerialization,
) {
    companion object {
        private const val VPN_REGIONS_FILENAME = "vpn-regions.json"
    }

    fun readVpnRegionsDetailsFromAssetsFolder(): List<VpnServer> = adaptVpnServers(readVpnRegionsResponse()).values.toList()

    fun readVpnRegionsDetailsAndServerInfoFromAssetsFolder(): Pair<List<VpnServer>, VpnServerInfo> {
        val vpnRegionsResponse = readVpnRegionsResponse()
        return adaptVpnServers(vpnRegionsResponse).values.toList() to adaptServersInfo(vpnRegionsResponse)
    }

    private fun readVpnRegionsResponse(): VpnRegionsResponse {
        val servers = regionInputStream.readAssetsFile(filename = VPN_REGIONS_FILENAME)
        return regionSerialization.decodeVpnRegionsFromString(
            servers.split("\n\n").first(),
        )
    }
}