package com.kape.region_selection.ui.vm

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
) : ViewModel() {

    private val _state = MutableStateFlow(IDLE)
    val state: StateFlow<RegionSelectionScreenState> = _state
    private var regions = mutableListOf<Server>()

    fun loadRegions(locale: String) = viewModelScope.launch {
        _state.emit(LOADING)
        getRegionsUseCase.loadRegions(locale).collect {
            regions.addAll(it)
            _state.emit(loaded(regions))
        }
    }

    fun initAutoRegion(name: String, iso: String) {
        regions.add(0, Server(
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
        ))
    }


    fun onRegionSelected(server: Server) {
        // TODO: handle region selection
    }

    fun showSortingOptions() = viewModelScope.launch {
        _state.emit(showFilteringOptions(regions))
    }

    fun hideSortingOptions() = viewModelScope.launch {
        _state.emit(loaded(regions))
    }
}