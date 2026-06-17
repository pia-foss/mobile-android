package com.kape.obfuscationregionselection.ui.vm

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kape.contracts.Router
import com.kape.data.DI
import com.kape.data.shadowsocksserver.ShadowsocksServer
import com.kape.localprefs.prefs.ShadowsocksRegionPrefs
import com.kape.obfuscationregionselection.util.ItemType
import com.kape.obfuscationregionselection.util.ShadowsocksServerItem
import com.kape.shadowsocksregions.domain.GetShadowsocksRegionsUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
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
    @Named(DI.IO_SCOPE) private val ioScope: CoroutineScope,
) : ViewModel() {
    val servers = mutableStateOf(emptyList<ShadowsocksServerItem>())
    val sorted = mutableStateOf(emptyList<ShadowsocksServerItem>())

    fun getShadowsocksRegions() =
        ioScope.launch {
            getShadowsocksRegionsUseCase.getShadowsocksServers().collectLatest {
                arrangeShadowsocksServers(it)
            }
        }

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
        if (value.isNotEmpty()) {
            isSearchEnabled.value = true
            sorted.value =
                servers.value.filter {
                    it.type is ItemType.Content &&
                        it.type.server.region
                            .lowercase()
                            .contains(value.lowercase())
                }
        } else {
            isSearchEnabled.value = false
        }
    }

    private fun isShadowsocksServerFavorite(serverName: String): Flow<Boolean> = shadowsocksRegionPrefs.isFavorite(serverName)

    private fun arrangeShadowsocksServers(items: List<ShadowsocksServer>? = null) =
        viewModelScope.launch(ioDispatcher) {
            val list = mutableListOf<ShadowsocksServerItem>()
            val favorites = mutableListOf<ShadowsocksServerItem>()
            val all = mutableListOf<ShadowsocksServerItem>()
            items?.let {
                for (server in it) {
                    if (isShadowsocksServerFavorite(server.region).first()) {
                        favorites.add(
                            ShadowsocksServerItem(
                                type =
                                    ItemType.Content(
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
                    if (isShadowsocksServerFavorite((item.type as ItemType.Content).server.region).first()) {
                        favorites.add(
                            ShadowsocksServerItem(
                                type =
                                    ItemType.Content(
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