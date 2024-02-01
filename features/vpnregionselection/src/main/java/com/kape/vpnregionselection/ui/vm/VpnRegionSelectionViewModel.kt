package com.kape.vpnregionselection.ui.vm

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kape.router.Back
import com.kape.router.ExitFlow
import com.kape.router.Router
import com.kape.utils.vpnserver.VpnServer
import com.kape.vpnconnect.domain.ConnectionUseCase
import com.kape.vpnregions.VpnRegionPrefs
import com.kape.vpnregions.domain.GetVpnRegionsUseCase
import com.kape.vpnregions.domain.ReadVpnRegionsDetailsUseCase
import com.kape.vpnregions.domain.UpdateLatencyUseCase
import com.kape.vpnregions.utils.VPN_REGIONS_PING_TIMEOUT
import com.kape.vpnregionselection.util.ItemType
import com.kape.vpnregionselection.util.ServerItem
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import java.util.Collections

class VpnRegionSelectionViewModel(
    private val getVpnRegionsUseCase: GetVpnRegionsUseCase,
    private val updateLatencyUseCase: UpdateLatencyUseCase,
    private val readVpnRegionsDetailsUseCase: ReadVpnRegionsDetailsUseCase,
    private val connectionUseCase: ConnectionUseCase,
    private val router: Router,
    private val vpnRegionPrefs: VpnRegionPrefs,
) : ViewModel(), KoinComponent {

    val servers = mutableStateOf(emptyList<ServerItem>())
    val sorted = mutableStateOf(emptyList<ServerItem>())
    lateinit var autoRegionName: String
    lateinit var autoRegionIso: String

    val sortBySelectedOption: MutableState<SortByOption> = mutableStateOf(SortByOption.NONE)

    fun loadInitialRegions() = viewModelScope.launch {
        arrangeVpnServers(readVpnRegionsDetailsUseCase.readVpnRegionsDetailsFromAssetsFolder())
    }

    fun loadVpnRegions(locale: String, isLoading: MutableState<Boolean>, displayLoading: Boolean) =
        viewModelScope.launch {
            if (displayLoading) {
                isLoading.value = true
            }
            getVpnRegionsUseCase.loadVpnServers(locale).collect {
                if (connectionUseCase.isConnected().not()) {
                    updateLatencyUseCase.updateLatencies().collect { updatedServers ->
                        for (server in updatedServers) {
                            it.filter { it.name == server.name }[0].latency =
                                server.latency ?: VPN_REGIONS_PING_TIMEOUT.toString()
                        }
                        arrangeVpnServers(it)
                        isLoading.value = false
                    }
                }
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
                if (isVpnServerFavorite(server.name)) {
                    favorites.add(
                        ServerItem(
                            type = ItemType.Content(
                                isFavorite = true,
                                server = server,
                            ),
                        ),
                    )
                } else {
                    all.add(
                        ServerItem(
                            ItemType.Content(
                                isFavorite = false,
                                server = server,
                            ),
                        ),
                    )
                }
            }
            all.add(0, autoRegion)
        } ?: run {
            for (item in servers.value.filter { it.type is ItemType.Content }) {
                if (isVpnServerFavorite((item.type as ItemType.Content).server.name)) {
                    favorites.add(
                        ServerItem(
                            type = ItemType.Content(
                                isFavorite = true,
                                server = item.type.server,
                            ),
                        ),
                    )
                } else {
                    all.add(
                        ServerItem(
                            ItemType.Content(
                                isFavorite = false,
                                enableFavorite = item.type.enableFavorite,
                                server = item.type.server,
                            ),
                        ),
                    )
                }
            }
        }

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
        list.addAll(all)
        servers.value = list
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
                    key = "",
                    latitude = null,
                    longitude = null,
                    isGeo = false,
                    isAllowsPF = false,
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