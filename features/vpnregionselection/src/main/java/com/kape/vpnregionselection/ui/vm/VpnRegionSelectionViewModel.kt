package com.kape.vpnregionselection.ui.vm

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kape.regions.data.ServerData
import com.kape.router.Back
import com.kape.router.EnterFlow
import com.kape.router.ExitFlow
import com.kape.router.Router
import com.kape.settings.SettingsPrefs
import com.kape.utils.AUTO_KEY
import com.kape.utils.vpnserver.VpnServer
import com.kape.vpnconnect.domain.ConnectionUseCase
import com.kape.vpnregions.VpnRegionPrefs
import com.kape.vpnregions.utils.RegionListProvider
import com.kape.vpnregionselection.util.ItemType
import com.kape.vpnregionselection.util.ServerItem
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import java.util.Collections

class VpnRegionSelectionViewModel(
    private val regionListProvider: RegionListProvider,
    private val connectionUseCase: ConnectionUseCase,
    private val router: Router,
    private val vpnRegionPrefs: VpnRegionPrefs,
    private val settingsPrefs: SettingsPrefs,
) : ViewModel(), KoinComponent {

    val servers = mutableStateOf(emptyList<ServerItem>())
    val sorted = mutableStateOf(emptyList<ServerItem>())
    lateinit var autoRegionName: String
    lateinit var autoRegionIso: String

    val isPortForwardingEnabled = settingsPrefs.isPortForwardingEnabled()

    fun loadVpnRegions(locale: String, isLoading: MutableState<Boolean>, displayLoading: Boolean) =
        viewModelScope.launch {
            arrangeVpnServers(regionListProvider.servers.value)
            if (displayLoading) {
                isLoading.value = true
            }
            regionListProvider.updateServerLatencies(
                locale,
                isVpnConnectionActive(),
                displayLoading,
            ).collect {
                arrangeVpnServers(it)
                isLoading.value = false
            }
        }

    fun onVpnRegionSelected(server: VpnServer) {
        vpnRegionPrefs.selectVpnServer(server)
        router.handleFlow(ExitFlow.RegionSelection)
    }

    fun onFavoriteVpnClicked(serverData: ServerData) {
        if (vpnRegionPrefs.isFavorite(serverData)) {
            vpnRegionPrefs.removeFromFavorites(serverData)
        } else {
            vpnRegionPrefs.addToFavorites(serverData)
        }
        arrangeVpnServers()
        updateVpnServers()
    }

    fun filterByName(value: String, isSearchEnabled: MutableState<Boolean>) =
        viewModelScope.launch {
            if (value.isNotEmpty()) {
                isSearchEnabled.value = true
                sorted.value = servers.value.filter {
                    it.type is ItemType.Content && it.type.server.name.lowercase()
                        .contains(value.lowercase())
                }
            } else {
                isSearchEnabled.value = false
            }
        }

    fun navigateToHelp() {
        router.handleFlow(EnterFlow.TvHelp)
    }

    fun navigateToSideMenu() {
        router.handleFlow(EnterFlow.TvSideMenu)
    }

    fun navigateToVpn() {
        router.handleFlow(EnterFlow.Connection)
    }

    fun navigateBack() {
        router.handleFlow(Back)
    }

    fun getTvVpnServers(): MutableState<List<ServerItem>> {
        var autoRegionIndex = servers.value.indexOfFirst { serverItem: ServerItem ->
            serverItem.type is ItemType.Content &&
                serverItem.type.server.iso == autoRegionIso &&
                serverItem.type.server.name == autoRegionName
        }
        if (autoRegionIndex == -1) {
            autoRegionIndex = 0
        }
        return mutableStateOf(servers.value.subList(autoRegionIndex, servers.value.size))
    }

    fun getTvSearchVpnServers(): MutableState<List<ServerItem>> {
        var autoRegionIndex = sorted.value.indexOfFirst { serverItem: ServerItem ->
            serverItem.type is ItemType.Content &&
                serverItem.type.server.iso == autoRegionIso &&
                serverItem.type.server.name == autoRegionName
        }
        if (autoRegionIndex == -1) {
            autoRegionIndex = 0
        } else {
            autoRegionIndex += 1
        }
        return mutableStateOf(sorted.value.subList(autoRegionIndex, sorted.value.size))
    }

    fun isVpnConnectionActive(): Boolean = connectionUseCase.isConnected()

    private fun isVpnServerFavorite(serverData: ServerData): Boolean {
        return vpnRegionPrefs.isFavorite(serverData)
    }

    private fun arrangeVpnServers(items: List<VpnServer>? = null) {
        val autoRegion = getAutoRegion(autoRegionName, autoRegionIso)
        val list = mutableListOf<ServerItem>()
        val favorites = mutableListOf<ServerItem>()
        val all = mutableListOf<ServerItem>()
        items?.let {
            for (server in it) {
                all.add(
                    ServerItem(
                        type = ItemType.Content(
                            isFavorite = isVpnServerFavorite(
                                ServerData(
                                    server.name,
                                    server.isDedicatedIp,
                                ),
                            ),
                            server = server,
                        ),
                    ),
                )
            }
            all.add(0, autoRegion)
        } ?: run {
            for (item in servers.value.filter { it.type is ItemType.Content }) {
                all.add(
                    ServerItem(
                        type = ItemType.Content(
                            isFavorite = isVpnServerFavorite(
                                ServerData(
                                    (item.type as ItemType.Content).server.name,
                                    item.type.server.isDedicatedIp,
                                ),
                            ),
                            enableFavorite = item.type.enableFavorite,
                            server = item.type.server,
                        ),
                    ),
                )
            }
        }

        favorites.addAll(all.filter { (it.type as ItemType.Content).isFavorite }.distinct())
        if (favorites.isNotEmpty()) {
            list.add(0, ServerItem(type = ItemType.HeadingFavorites))
            favorites.sortBy { (it.type as ItemType.Content).server.latency?.toInt() }
            list.addAll(favorites)
            list.add(ServerItem(type = ItemType.HeadingAll))
        }

        all.sortBy { (it.type as ItemType.Content).server.latency?.toInt() }
        all.sortByDescending { (it.type as ItemType.Content).server.isDedicatedIp }
        Collections.swap(
            all,
            0,
            all.indexOf(all.filter { it.type is ItemType.Content && it.type.server.name == autoRegionName }[0]),
        )
        list.addAll(all.distinct())
        servers.value = list
    }

    private fun updateVpnServers() {
        val updatedList = mutableListOf<ServerItem>()
        for (item in sorted.value) {
            val current = item.type as ItemType.Content
            updatedList.add(servers.value.first { it.type is ItemType.Content && it.type.server.name == current.server.name })
        }
        sorted.value = updatedList
    }

    private fun getAutoRegion(name: String, iso: String): ServerItem {
        return ServerItem(
            type = ItemType.Content(
                isFavorite = false,
                enableFavorite = false,
                server = VpnServer(
                    name = name,
                    iso = iso,
                    dns = "",
                    latency = null,
                    endpoints = emptyMap(),
                    key = AUTO_KEY,
                    latitude = null,
                    longitude = null,
                    isGeo = false,
                    allowsPortForwarding = false,
                    isOffline = false,
                    autoRegion = true,
                    dipToken = null,
                    dedicatedIp = null,
                ),
            ),
        )
    }
}