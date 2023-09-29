package com.kape.regionselection.ui.vm

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toLowerCase
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kape.regions.RegionPrefs
import com.kape.regionselection.utils.IDLE
import com.kape.regionselection.utils.LOADING
import com.kape.regionselection.utils.RegionSelectionScreenState
import com.kape.regionselection.utils.loaded
import com.kape.regionselection.utils.showFilteringOptions
import com.kape.router.Back
import com.kape.router.ExitFlow
import com.kape.router.Router
import com.kape.regions.domain.GetRegionsUseCase
import com.kape.regions.domain.UpdateLatencyUseCase
import com.kape.regions.utils.REGIONS_PING_TIMEOUT
import com.kape.utils.server.Server
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent

class RegionSelectionViewModel(
    private val getRegionsUseCase: GetRegionsUseCase,
    private val updateLatencyUseCase: UpdateLatencyUseCase,
    private val router: Router,
    private val prefs: RegionPrefs,
) : ViewModel(), KoinComponent {

    private val _state = MutableStateFlow(IDLE)
    val state: StateFlow<RegionSelectionScreenState> = _state
    private var regions = mutableListOf<Server>()
    private var sorted = emptyList<Server>()
    val sortBySelectedOption: MutableState<SortByOption> = mutableStateOf(SortByOption.NONE)

    fun loadRegions(locale: String) = viewModelScope.launch {
        _state.emit(LOADING)
        getRegionsUseCase.loadRegions(locale).collect {
            regions.clear()
            regions.addAll(it)
            updateLatencyUseCase.updateLatencies().collect { updatedServers ->
                for (server in updatedServers) {
                    sorted.ifEmpty { regions }.filter { it.name == server.name }[0].latency =
                        server.latency ?: REGIONS_PING_TIMEOUT.toString()
                }
                _state.emit(loaded(sorted.ifEmpty { regions }, state.value.showSortingOptions))
            }
        }
    }

    fun initAutoRegion(name: String, iso: String) {
        if (regions.none { it.name == name }) {
            regions.add(
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
    }

    fun isServerFavorite(serverName: String): Boolean {
        return prefs.isFavorite(serverName)
    }

    fun showSortingOptions() = viewModelScope.launch {
        _state.emit(showFilteringOptions(sorted.ifEmpty { regions }))
    }

    fun hideSortingOptions() = viewModelScope.launch {
        _state.emit(loaded(sorted.ifEmpty { regions }))
    }

    fun filterByName(value: String) = viewModelScope.launch {
        if (value.isEmpty()) {
            _state.emit(loaded(sorted.ifEmpty { regions }, state.value.showSortingOptions))
        } else {
            _state.emit(
                loaded(
                    sorted.ifEmpty { regions }
                        .filter {
                            it.name.toLowerCase(Locale.current)
                                .contains(value.toLowerCase(Locale.current))
                        },
                ),
            )
        }
    }

    fun sortBy(option: SortByOption) = viewModelScope.launch {
        sorted = when (option) {
            SortByOption.NONE -> regions
            SortByOption.NAME -> regions.sortedBy { it.name }
            SortByOption.LATENCY -> regions.sortedBy { it.latency?.toInt() }
            SortByOption.FAVORITE -> {
                regions.sortedWith(compareBy<Server> { isServerFavorite(it.name) }.thenBy { it.name })
                    .sortedByDescending { isServerFavorite(it.name) }
            }
        }
        _state.emit(loaded(sorted))
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

    sealed class SortByOption(val index: Int) {
        object NONE : SortByOption(-1)
        object NAME : SortByOption(0)
        object LATENCY : SortByOption(1)
        object FAVORITE : SortByOption(2)
    }
}