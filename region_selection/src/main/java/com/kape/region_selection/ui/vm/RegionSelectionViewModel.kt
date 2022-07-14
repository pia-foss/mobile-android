package com.kape.region_selection.ui.vm

import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toLowerCase
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kape.region_selection.domain.GetRegionsUseCase
import com.kape.region_selection.domain.UpdateLatencyUseCase
import com.kape.region_selection.server.Server
import com.kape.region_selection.utils.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RegionSelectionViewModel(
    private val getRegionsUseCase: GetRegionsUseCase,
    private val updateLatencyUseCase: UpdateLatencyUseCase,
    private val prefs: RegionPrefs
) : ViewModel() {

    private val _state = MutableStateFlow(IDLE)
    val state: StateFlow<RegionSelectionScreenState> = _state
    private var regions = mutableListOf<Server>()

    fun loadRegions(locale: String) = viewModelScope.launch {
        _state.emit(LOADING)
        getRegionsUseCase.loadRegions(locale).collect {
            regions.addAll(it)
            updateLatencies()
            _state.emit(loaded(regions))
        }
    }

    fun initAutoRegion(name: String, iso: String) {
        if (regions.none { it.name == name }) {
            regions.add(
                0, Server(
                    name = name,
                    iso = iso,
                    dns = "",
                    latency = null,
                    endpoints = emptyMap(),
                    key = "",
                    latitude = null,
                    longitude = null,
                    isGeo = false,
                    isAllowsPF = false,
                    isOffline = false,
                    dipToken = null,
                    dedicatedIp = null
                )
            )
        }
    }

    private fun updateLatencies() = viewModelScope.launch {
        updateLatencyUseCase.updateLatencies().collect { updatedServers ->
            for (server in updatedServers) {
                regions.filter { it.name == server.name }[0].latency = server.latency
            }
            _state.emit(loaded(regions, state.value.showSortingOptions))
        }
    }

    fun onRegionSelected(server: Server) {
        // TODO: handle region selection
    }

    fun onFavoriteClicked(serverName: String) {
        if (prefs.isFavorite(serverName)) {
            prefs.removeFromFavorites(serverName)
        } else {
            prefs.addToFavorites(serverName)
        }
    }

    fun isServerFavorite(serverName: String): Boolean {
        return prefs.isFavorite(serverName)
    }

    fun showSortingOptions() = viewModelScope.launch {
        _state.emit(showFilteringOptions(regions))
    }

    fun hideSortingOptions() = viewModelScope.launch {
        _state.emit(loaded(regions))
    }

    fun filterByName(value: String) = viewModelScope.launch {
        if (value.isEmpty()) {
            _state.emit(loaded(regions, state.value.showSortingOptions))
        } else {
            _state.emit(loaded(regions.filter { it.name.toLowerCase(Locale.current).contains(value.toLowerCase(Locale.current)) }))
        }
    }
}