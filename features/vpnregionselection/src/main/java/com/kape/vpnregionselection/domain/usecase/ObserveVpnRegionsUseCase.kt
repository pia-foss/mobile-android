package com.kape.vpnregionselection.domain.usecase

import com.kape.data.AUTO_KEY
import com.kape.data.RegionServerItem
import com.kape.data.arrangeRegions
import com.kape.data.vpnserver.VpnServer
import com.kape.localprefs.api.SettingsStore
import com.kape.localprefs.api.VpnRegionStore
import com.kape.regions.data.ServerData
import com.kape.regions.vpn.domain.usecase.ObserveVpnServersUseCase
import com.kape.vpnregionselection.domain.isListed
import com.kape.vpnregionselection.domain.serverGroupFor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import org.koin.core.annotation.Factory

@Factory
class ObserveVpnRegionsUseCase(
    private val observeVpnServers: ObserveVpnServersUseCase,
    private val vpnRegionPrefs: VpnRegionStore,
    private val settingsPrefs: SettingsStore,
) {
    operator fun invoke(): Flow<List<RegionServerItem<VpnServer>>> =
        combine(
            observeVpnServers(),
            vpnRegionPrefs.favoriteVpnServers,
            settingsPrefs.selectedProtocol,
            settingsPrefs.openVpnSettings,
            settingsPrefs.isShowGeoLocatedServersEnabled,
        ) { servers, favorites, protocol, openVpnSettings, showGeoLocated ->
            val serverGroup = serverGroupFor(protocol, openVpnSettings.transport)
            val listed =
                servers
                    .filter { it.key != AUTO_KEY && it.isListed(serverGroup, showGeoLocated) }
                    .sortedWith(compareByDescending<VpnServer> { it.isDedicatedIp }.thenBy { it.latency?.toIntOrNull() })
            arrangeRegions(
                servers = listOf(AUTO_REGION) + listed,
                isFavorite = { ServerData(it.name, it.isDedicatedIp) in favorites },
                canBeFavorite = { it.key != AUTO_KEY },
            )
        }
}

internal val AUTO_REGION =
    VpnServer(
        name = "",
        iso = "",
        dns = "",
        latency = null,
        endpoints = emptyMap(),
        key = AUTO_KEY,
        latitude = null,
        longitude = null,
        isGeo = false,
        isOffline = false,
        allowsPortForwarding = false,
        autoRegion = true,
        dipToken = null,
        dedicatedIp = null,
    )