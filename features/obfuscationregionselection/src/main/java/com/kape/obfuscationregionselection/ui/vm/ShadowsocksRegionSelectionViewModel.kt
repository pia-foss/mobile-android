package com.kape.obfuscationregionselection.ui.vm

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kape.contracts.Router
import com.kape.data.DI
import com.kape.data.RegionItemType
import com.kape.data.RegionServerItem
import com.kape.data.shadowsocksserver.ShadowsocksServer
import com.kape.localprefs.prefs.ShadowsocksRegionPrefs
import com.kape.shadowsocksregions.domain.GetShadowsocksRegionsUseCase
import com.kape.utils.arrangeServers
import com.kape.utils.filterServersByName
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import org.koin.core.annotation.Named

@KoinViewModel
class ShadowsocksRegionSelectionViewModel(
    private val router: Router,
    private val getShadowsocksRegionsUseCase: GetShadowsocksRegionsUseCase,
    private val shadowsocksRegionPrefs: ShadowsocksRegionPrefs,
    @Named(DI.IO_DISPATCHER) private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {
    val servers = mutableStateOf(emptyList<RegionServerItem<ShadowsocksServer>>())
    val sorted = mutableStateOf(emptyList<RegionServerItem<ShadowsocksServer>>())

    fun getShadowsocksRegions() = arrangeShadowsocksServers(getShadowsocksRegionsUseCase.getShadowsocksServers())

    fun fetchShadowsocksRegions(
        locale: String,
        isLoading: MutableState<Boolean>,
    ) = viewModelScope.launch(ioDispatcher) {
        isLoading.value = true
        val servers = getShadowsocksRegionsUseCase.fetchShadowsocksServers(locale)
        arrangeShadowsocksServers(servers)
        isLoading.value = false
    }

    fun onShadowsocksRegionSelected(server: ShadowsocksServer) =
        viewModelScope.launch(ioDispatcher) {
            shadowsocksRegionPrefs.setSelectShadowsocksServer(server)
        }

    fun onFavoriteShadowsocksClicked(serverName: String) =
        viewModelScope.launch(ioDispatcher) {
            if (shadowsocksRegionPrefs.isFavorite(serverName).first()) {
                shadowsocksRegionPrefs.removeFromFavorites(serverName)
            } else {
                shadowsocksRegionPrefs.addToFavorites(serverName)
            }
            arrangeShadowsocksServers()
        }

    fun filterByName(
        value: String,
        isSearchEnabled: MutableState<Boolean>,
    ) = viewModelScope.launch(ioDispatcher) {
        isSearchEnabled.value = value.isNotEmpty()
        sorted.value =
            filterServersByName(servers.value, value) { item ->
                (item.type as? RegionItemType.Content)?.server?.region
            }
    }

    private fun isShadowsocksServerFavorite(serverName: String): Flow<Boolean> = shadowsocksRegionPrefs.isFavorite(serverName)

    private fun arrangeShadowsocksServers(items: List<ShadowsocksServer>? = null) =
        viewModelScope.launch(ioDispatcher) {
            servers.value =
                arrangeServers(
                    items = items,
                    currentItems = servers.value,
                    toServer = { item -> (item.type as? RegionItemType.Content)?.server },
                    isFavorite = { server -> isShadowsocksServerFavorite(server.region).first() },
                    toItem = { server, favorite ->
                        RegionServerItem(type = RegionItemType.Content(isFavorite = favorite, server = server))
                    },
                    headingFavorites = RegionServerItem(type = RegionItemType.HeadingFavorites),
                    headingAll = RegionServerItem(type = RegionItemType.HeadingAll),
                    includeFavoritesInAll = false,
                )
        }
}