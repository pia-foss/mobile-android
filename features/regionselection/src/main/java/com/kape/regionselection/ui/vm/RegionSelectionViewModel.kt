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

class RegionSelectionViewModel(
    private val getRegionsUseCase: GetRegionsUseCase,
    private val updateLatencyUseCase: UpdateLatencyUseCase,
    private val router: Router,
    private val prefs: RegionPrefs,
) : ViewModel(), KoinComponent {

    val servers = mutableStateOf(emptyList<ServerItem>())
    val sorted = mutableStateOf(emptyList<ServerItem>())

    private val regions = mutableStateOf(emptyList<Server>())

    val sortBySelectedOption: MutableState<SortByOption> = mutableStateOf(SortByOption.NONE)

    fun loadRegions(locale: String, isLoading: MutableState<Boolean>) = viewModelScope.launch {
        isLoading.value = true
        getRegionsUseCase.loadRegions(locale).collect { it ->
            regions.value = it
            updateLatencyUseCase.updateLatencies().collect { updatedServers ->
                for (server in updatedServers) {
                    regions.value.filter { it.name == server.name }[0].latency =
                        server.latency ?: REGIONS_PING_TIMEOUT.toString()
                }
                arrangeServers()
                isLoading.value = false
            }
        }
    }

    fun initAutoRegion(name: String, iso: String) {
        if (regions.value.none { it.name == name }) {
            val updatedList = mutableListOf<Server>()
            updatedList.add(
                0,
                Server(
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
            )
            updatedList.addAll(regions.value)
            regions.value = updatedList
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
        regions.value = when (option) {
            SortByOption.NONE -> regions.value
            SortByOption.NAME -> regions.value.sortedBy { it.name }
            SortByOption.LATENCY -> regions.value.sortedBy { it.latency?.toInt() }
            SortByOption.FAVORITE -> {
                regions.value.sortedWith(compareBy<Server> { isServerFavorite(it.name) }.thenBy { it.name })
                    .sortedByDescending { isServerFavorite(it.name) }
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

    private fun arrangeServers() {
        val list = mutableListOf<ServerItem>()
        val favorites = mutableListOf<ServerItem>()
        val all = mutableListOf<ServerItem>()
        for (server in regions.value) {
            if (isServerFavorite(server.name)) {
                favorites.add(ServerItem(type = ItemType.Content(true, server = server)))
            } else {
                all.add(ServerItem(ItemType.Content(false, server = server)))
            }
        }
        if (favorites.isNotEmpty()) {
            list.add(0, ServerItem(type = ItemType.HeadingFavorites))
            list.addAll(favorites)
            list.add(ServerItem(type = ItemType.HeadingAll))
            list.addAll(all)
        } else {
            list.addAll(all)
        }
        servers.value = list
    }

    sealed class SortByOption(val index: Int) {
        data object NONE : SortByOption(-1)
        data object NAME : SortByOption(0)
        data object LATENCY : SortByOption(1)
        data object FAVORITE : SortByOption(2)
    }
}