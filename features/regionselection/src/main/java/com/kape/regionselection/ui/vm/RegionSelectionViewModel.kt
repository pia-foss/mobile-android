package com.kape.regionselection.ui.vm

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kape.regions.RegionPrefs
import com.kape.regions.domain.GetRegionsUseCase
import com.kape.regions.domain.UpdateLatencyUseCase
import com.kape.regions.utils.REGIONS_PING_TIMEOUT
import com.kape.regionselection.util.ItemType
import com.kape.regionselection.util.ServerItem
import com.kape.router.Back
import com.kape.router.ExitFlow
import com.kape.router.Router
import com.kape.utils.server.Server
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import java.util.Collections

class RegionSelectionViewModel(
    private val getRegionsUseCase: GetRegionsUseCase,
    private val updateLatencyUseCase: UpdateLatencyUseCase,
    private val router: Router,
    private val prefs: RegionPrefs,
) : ViewModel(), KoinComponent {

    val servers = mutableStateOf(emptyList<ServerItem>())
    val sorted = mutableStateOf(emptyList<ServerItem>())
    lateinit var autoRegionName: String
    lateinit var autoRegionIso: String

    val sortBySelectedOption: MutableState<SortByOption> = mutableStateOf(SortByOption.NONE)

    fun loadRegions(locale: String, isLoading: MutableState<Boolean>) = viewModelScope.launch {
        isLoading.value = true
        getRegionsUseCase.loadRegions(locale).collect {
            updateLatencyUseCase.updateLatencies().collect { updatedServers ->
                for (server in updatedServers) {
                    it.filter { it.name == server.name }[0].latency =
                        server.latency ?: REGIONS_PING_TIMEOUT.toString()
                }
                arrangeServers(it)
                isLoading.value = false
            }
        }
    }

    fun onRegionSelected(server: Server) {
        prefs.selectServer(server.key)
        router.handleFlow(ExitFlow.RegionSelection)
    }

    fun onFavoriteClicked(serverName: String) {
        if (prefs.isFavorite(serverName)) {
            prefs.removeFromFavorites(serverName)
        } else {
            prefs.addToFavorites(serverName)
        }
        arrangeServers()
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
                    .sortedByDescending { isServerFavorite((it.type as ItemType.Content).server.name) }
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

    private fun isServerFavorite(serverName: String): Boolean {
        return prefs.isFavorite(serverName)
    }

    private fun arrangeServers(items: List<Server>? = null) {
        val autoRegion = getAutoRegion(autoRegionName, autoRegionIso)
        val list = mutableListOf<ServerItem>()
        val favorites = mutableListOf<ServerItem>()
        val all = mutableListOf<ServerItem>()
        items?.let {
            for (server in it) {
                if (isServerFavorite(server.name)) {
                    favorites.add(ServerItem(type = ItemType.Content(true, server = server)))
                } else {
                    all.add(ServerItem(ItemType.Content(false, server = server)))
                }
            }
            all.add(0, autoRegion)
        } ?: run {
            for (item in servers.value.filter { it.type is ItemType.Content }) {
                if (isServerFavorite((item.type as ItemType.Content).server.name)) {
                    favorites.add(
                        ServerItem(
                            type = ItemType.Content(
                                true,
                                server = item.type.server,
                            ),
                        ),
                    )
                } else {
                    all.add(ServerItem(ItemType.Content(false, server = item.type.server)))
                }
            }
        }

        if (favorites.isNotEmpty()) {
            list.add(0, ServerItem(type = ItemType.HeadingFavorites))
            list.addAll(favorites)
            list.add(ServerItem(type = ItemType.HeadingAll))
        }
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
                server = Server(
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