package com.kape.vpnregionselection.ui.vm

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kape.router.Back
import com.kape.router.ExitFlow
import com.kape.router.Router
import com.kape.settings.SettingsPrefs
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

    val sortBySelectedOption: MutableState<SortByOption> = mutableStateOf(SortByOption.NONE)
    val isPortForwardingEnabled = settingsPrefs.isPortForwardingEnabled()

    fun loadVpnRegions(locale: String, isLoading: MutableState<Boolean>, displayLoading: Boolean) =
        viewModelScope.launch {
            arrangeVpnServers(regionListProvider.servers.value)
            if (displayLoading) {
                isLoading.value = true
            }
            regionListProvider.updateServerList(locale, connectionUseCase.isConnected()).collect {
                arrangeVpnServers(it)
                isLoading.value = false
            }
        }

    fun onVpnRegionSelected(server: VpnServer) {
        vpnRegionPrefs.selectVpnServer(server.key)
        router.handleFlow(ExitFlow.RegionSelection)
    }

    fun onFavoriteVpnClicked(serverName: String) {
        if (vpnRegionPrefs.isFavorite(serverName)) {
            vpnRegionPrefs.removeFromFavorites(serverName)
        } else {
            vpnRegionPrefs.addToFavorites(serverName)
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

    fun sortBy(option: SortByOption) = viewModelScope.launch {
        servers.value = when (option) {
            SortByOption.NONE -> servers.value
            SortByOption.NAME -> servers.value.filter { it.type is ItemType.Content }
                .sortedBy { (it.type as ItemType.Content).server.name }

            SortByOption.LATENCY -> servers.value.filter { it.type is ItemType.Content }
                .sortedBy { (it.type as ItemType.Content).server.latency?.toInt() }

            SortByOption.FAVORITE -> {
                servers.value.filter { it.type is ItemType.Content }
                    .sortedWith(compareBy<ServerItem> { (it.type as ItemType.Content).isFavorite }.thenBy { (it.type as ItemType.Content).server.name })
                    .sortedByDescending { isVpnServerFavorite((it.type as ItemType.Content).server.name) }
            }
        }.toList()
    }

    fun getSortingOption(selected: Int): SortByOption {
        return when (selected) {
            SortByOption.NONE.index -> SortByOption.NONE
            SortByOption.NAME.index -> SortByOption.NAME
            SortByOption.LATENCY.index -> SortByOption.LATENCY
            SortByOption.FAVORITE.index -> SortByOption.FAVORITE
            else -> throw Exception("Sorting option not defined")
        }
    }

    fun navigateBack() {
        router.handleFlow(Back)
    }

    private fun isVpnServerFavorite(serverName: String): Boolean {
        return vpnRegionPrefs.isFavorite(serverName)
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
                            isFavorite = isVpnServerFavorite(server.name),
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
                            isFavorite = isVpnServerFavorite((item.type as ItemType.Content).server.name),
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
                    latency = null,
                    endpoints = emptyMap(),
                    key = "auto",
                    latitude = null,
                    longitude = null,
                    isGeo = false,
                    allowsPortForwarding = false,
                    isOffline = false,
                    dipToken = null,
                    dedicatedIp = null,
                ),
            ),
        )
    }

    sealed class SortByOption(val index: Int) {
        data object NONE : SortByOption(-1)
        data object NAME : SortByOption(0)
        data object LATENCY : SortByOption(1)
        data object FAVORITE : SortByOption(2)
    }
}