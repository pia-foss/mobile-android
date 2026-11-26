package com.kape.obfuscationregionselection.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kape.contracts.Router
import com.kape.data.RegionServerItem
import com.kape.data.arrangeRegions
import com.kape.data.searchByName
import com.kape.data.shadowsocksserver.ShadowsocksServer
import com.kape.localprefs.api.ShadowsocksRegionStore
import com.kape.regions.shadowsocks.domain.usecase.ObserveShadowsocksServersUseCase
import com.kape.regions.shadowsocks.domain.usecase.RefreshShadowsocksServersUseCase
import com.kape.regions.shadowsocks.domain.usecase.SelectShadowsocksServerUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

data class ShadowsocksRegionsState(
    val regions: List<RegionServerItem<ShadowsocksServer>> = emptyList(),
    val searchResults: List<RegionServerItem<ShadowsocksServer>>? = null,
    val isRefreshing: Boolean = false,
) {
    val visibleRegions: List<RegionServerItem<ShadowsocksServer>> get() = searchResults ?: regions
}

@KoinViewModel
class ShadowsocksRegionSelectionViewModel(
    observeShadowsocksServers: ObserveShadowsocksServersUseCase,
    private val refreshShadowsocksServers: RefreshShadowsocksServersUseCase,
    private val selectShadowsocksServer: SelectShadowsocksServerUseCase,
    private val shadowsocksRegionPrefs: ShadowsocksRegionStore,
    private val router: Router,
) : ViewModel() {
    private val query = MutableStateFlow("")
    private val isRefreshing = MutableStateFlow(false)

    val state: StateFlow<ShadowsocksRegionsState> =
        combine(
            observeShadowsocksServers(),
            shadowsocksRegionPrefs.observeFavorites(),
            query,
            isRefreshing,
        ) { servers, favorites, query, isRefreshing ->
            val regions = arrangeRegions(servers, isFavorite = { it.region in favorites }, includeFavoritesInAll = false)
            ShadowsocksRegionsState(
                regions = regions,
                searchResults = query.takeIf { it.isNotEmpty() }?.let { regions.searchByName(it) { server -> server.region } },
                isRefreshing = isRefreshing,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), ShadowsocksRegionsState())

    fun refresh(locale: String) {
        viewModelScope.launch {
            isRefreshing.value = true
            try {
                refreshShadowsocksServers(locale)
            } finally {
                isRefreshing.value = false
            }
        }
    }

    fun search(query: String) {
        this.query.value = query
    }

    /** Saving first, as closing the screen cancels this view model's work. */
    fun onRegionClicked(server: ShadowsocksServer) {
        viewModelScope.launch {
            selectShadowsocksServer(server)
            router.navigateBack()
        }
    }

    fun onFavoriteClicked(server: ShadowsocksServer) {
        viewModelScope.launch {
            if (shadowsocksRegionPrefs.isFavorite(server.region).first()) {
                shadowsocksRegionPrefs.removeFromFavorites(server.region)
            } else {
                shadowsocksRegionPrefs.addToFavorites(server.region)
            }
        }
    }
}

private const val STOP_TIMEOUT_MS = 5_000L