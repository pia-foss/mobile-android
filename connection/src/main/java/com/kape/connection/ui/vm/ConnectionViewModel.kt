package com.kape.connection.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kape.connection.utils.ConnectionScreenState
import com.kape.connection.utils.IDLE
import com.kape.region_selection.domain.GetRegionsUseCase
import com.kape.region_selection.domain.UpdateLatencyUseCase
import com.kape.region_selection.server.Server
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// TODO: temporary solution, will expand
class ConnectionViewModel(
    private val regionsUseCase: GetRegionsUseCase,
    private val updateLatencyUseCase: UpdateLatencyUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(IDLE)
    val state: StateFlow<ConnectionScreenState> = _state

    private var availableServers = mutableListOf<Server>()

    fun loadServers(locale: String) = viewModelScope.launch {
        regionsUseCase.loadRegions(locale).collect {
            availableServers.clear()
            updateLatencyUseCase.updateLatencies().collect {
                availableServers.addAll(it)
                getSelectedServer()
            }
        }
    }

    private fun getSelectedServer() = viewModelScope.launch {
        if (availableServers.isNotEmpty()) {
            val selectedServer =
                availableServers.firstOrNull { it.key == regionsUseCase.getSelectedRegion() }
                    ?: availableServers.sortedBy { it.latency?.toInt() }.first()
            regionsUseCase.selectRegion(selectedServer.key)
            _state.emit(ConnectionScreenState(selectedServer))
        }
    }
}