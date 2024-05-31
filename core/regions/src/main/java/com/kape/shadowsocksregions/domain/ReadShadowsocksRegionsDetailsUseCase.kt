package com.kape.shadowsocksregions.domain

import com.kape.data.RegionInputStream
import com.kape.data.RegionSerialization
import com.kape.shadowsocksregions.utils.adaptShadowsocksServers
import com.kape.utils.shadowsocksserver.ShadowsocksServer
import com.kape.vpnregions.domain.ReadVpnRegionsDetailsUseCase

class ReadShadowsocksRegionsDetailsUseCase(
    private val regionInputStream: RegionInputStream,
    private val regionSerialization: RegionSerialization,
    private val readVpnRegionsDetailsUseCase: ReadVpnRegionsDetailsUseCase,
) {

    companion object {
        private const val SHADOWSOCKS_REGIONS_FILENAME = "shadowsocks-regions.json"
    }

    fun readShadowsocksRegionsDetailsFromAssetsFolder(): List<ShadowsocksServer> {
        val servers = regionInputStream.readAssetsFile(filename = SHADOWSOCKS_REGIONS_FILENAME)
        val shadowSocksRegionsResponse = regionSerialization.decodeShadowsocksRegionsFromString(
            servers.split("\n\n").first(),
        )

        // The shadowsocks API only offers the regions id which we then need to use with the
        // vpn regions response to get the details for that particular region.
        val shadowsocksServers = adaptShadowsocksServers(shadowSocksRegionsResponse)
        val vpnServers = readVpnRegionsDetailsUseCase.readVpnRegionsDetailsFromAssetsFolder()
        val updatedShadowsocksServers = mutableListOf<ShadowsocksServer>()
        for (shadowsocksServer in shadowsocksServers) {
            vpnServers.firstOrNull { it.key.equals(shadowsocksServer.region, ignoreCase = true) }?.let {
                updatedShadowsocksServers.add(
                    shadowsocksServer.copy(
                        iso = it.iso,
                        region = it.name,
                    ),
                )
            }
        }

        return updatedShadowsocksServers
    }
}