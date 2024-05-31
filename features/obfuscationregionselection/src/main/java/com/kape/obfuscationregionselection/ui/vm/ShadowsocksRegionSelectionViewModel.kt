package com.kape.obfuscationregionselection.ui.vm

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kape.obfuscationregionselection.util.ItemType
import com.kape.obfuscationregionselection.util.ShadowsocksServerItem
import com.kape.router.Back
import com.kape.router.ExitFlow
import com.kape.router.Router
import com.kape.shadowsocksregions.ShadowsocksRegionPrefs
import com.kape.shadowsocksregions.domain.GetShadowsocksRegionsUseCase
import com.kape.utils.shadowsocksserver.ShadowsocksServer
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent

class ShadowsocksRegionSelectionViewModel(
    private val getShadowsocksRegionsUseCase: GetShadowsocksRegionsUseCase,
    private val router: Router,
    private val shadowsocksRegionPrefs: ShadowsocksRegionPrefs,
) : ViewModel(), KoinComponent {

    val servers = mutableStateOf(emptyList<ShadowsocksServerItem>())
    val sorted = mutableStateOf(emptyList<ShadowsocksServerItem>())

    fun getShadowsocksRegions() =
        arrangeShadowsocksServers(getShadowsocksRegionsUseCase.getShadowsocksServers())

    fun fetchShadowsocksRegions(locale: String, isLoading: MutableState<Boolean>) = viewModelScope.launch {
        isLoading.value = true
        getShadowsocksRegionsUseCase.fetchShadowsocksServers(locale).collect {
            arrangeShadowsocksServers(it)
            isLoading.value = false
        }
    }

    fun onShadowsocksRegionSelected(server: ShadowsocksServer) {
        shadowsocksRegionPrefs.setSelectShadowsocksServer(server)
        router.handleFlow(ExitFlow.RegionSelection)
    }

    fun onFavoriteShadowsocksClicked(serverName: String) {
        if (shadowsocksRegionPrefs.isFavorite(serverName)) {
            shadowsocksRegionPrefs.removeFromFavorites(serverName)
        } else {
            shadowsocksRegionPrefs.addToFavorites(serverName)
        }
        arrangeShadowsocksServers()
    }

    fun filterByName(value: String, isSearchEnabled: MutableState<Boolean>) =
        viewModelScope.launch {
            if (value.isNotEmpty()) {
                isSearchEnabled.value = true
                sorted.value = servers.value.filter {
                    it.type is ItemType.Content && it.type.server.region.lowercase()
                        .contains(value.lowercase())
                }
            } else {
                isSearchEnabled.value = false
            }
        }

    fun navigateBack() {
        router.handleFlow(Back)
    }

    private fun isShadowsocksServerFavorite(serverName: String): Boolean {
        return shadowsocksRegionPrefs.isFavorite(serverName)
    }

    private fun arrangeShadowsocksServers(items: List<ShadowsocksServer>? = null) {
        val list = mutableListOf<ShadowsocksServerItem>()
        val favorites = mutableListOf<ShadowsocksServerItem>()
        val all = mutableListOf<ShadowsocksServerItem>()
        items?.let {
            for (server in it) {
                if (isShadowsocksServerFavorite(server.region)) {
                    favorites.add(
                        ShadowsocksServerItem(
                            type = ItemType.Content(
                                isFavorite = true,
                                server = server,
                            ),
                        ),
                    )
                } else {
                    all.add(
                        ShadowsocksServerItem(
                            ItemType.Content(
                                isFavorite = false,
                                server = server,
                            ),
                        ),
                    )
                }
            }
        } ?: run {
            for (item in servers.value.filter { it.type is ItemType.Content }) {
                if (isShadowsocksServerFavorite((item.type as ItemType.Content).server.region)) {
                    favorites.add(
                        ShadowsocksServerItem(
                            type = ItemType.Content(
                                isFavorite = true,
                                server = item.type.server,
                            ),
                        ),
                    )
                } else {
                    all.add(
                        ShadowsocksServerItem(
                            ItemType.Content(
                                isFavorite = false,
                                server = item.type.server,
                            ),
                        ),
                    )
                }
            }
        }

        if (favorites.isNotEmpty()) {
            list.add(0, ShadowsocksServerItem(type = ItemType.HeadingFavorites))
            list.addAll(favorites)
            list.add(ShadowsocksServerItem(type = ItemType.HeadingAll))
        }
        list.addAll(all)
        servers.value = list
    }
}