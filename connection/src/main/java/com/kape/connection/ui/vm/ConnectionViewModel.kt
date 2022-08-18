package com.kape.connection.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kape.connection.utils.*
import com.kape.region_selection.domain.GetRegionsUseCase
import com.kape.region_selection.domain.UpdateLatencyUseCase
import com.kape.region_selection.server.Server
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter

// TODO: temporary solution, will expand
class ConnectionViewModel(
    private val regionsUseCase: GetRegionsUseCase,
    private val updateLatencyUseCase: UpdateLatencyUseCase,
) : ViewModel() {

    private val oneHourLong = 1L
    private val fiveMinuteLong = 5L
    private val fifteenMinuteLong = 15L

    val SNOOZE_DEFAULT_MS = 0
    val SNOOZE_SHORT_MS = 5 * 60 * 1000
    val SNOOZE_MEDIUM_MS = 15 * 60 * 1000
    val SNOOZE_LONG_MS = 60 * 60 * 1000

    private val _state = MutableStateFlow(IDLE)
    val state: StateFlow<ConnectionScreenState> = _state

    private var availableServers = mutableListOf<Server>()
    private var selectedServer: Server? = null
    private var usageState: UsageState = USAGE_STATE_DEFAULT

    fun loadServers(locale: String) = viewModelScope.launch {
        regionsUseCase.loadRegions(locale).collect {
            availableServers.clear()
            updateLatencyUseCase.updateLatencies().collect {
                availableServers.addAll(it)
                getSelectedServer()
            }
        }
    }

    fun snooze(interval: Int) {
        val newState: ConnectionScreenState
        val formatter = DateTimeFormatter.ofPattern("HH:mm")
        val now = LocalTime.now()
        // TODO: implement missing logic
        when (interval) {
            SNOOZE_SHORT_MS -> {
                val end = now.plusMinutes(fiveMinuteLong)
                newState = ConnectionScreenState(
                    selectedServer,
                    SnoozeState(
                        active = true,
                        formatter.format(end)
                    ), usageState
                )
            }
            SNOOZE_MEDIUM_MS -> {
                val end = now.plusMinutes(fifteenMinuteLong)
                newState = ConnectionScreenState(
                    selectedServer, SnoozeState(
                        active = true,
                        formatter.format(end)
                    ), usageState
                )
            }
            SNOOZE_LONG_MS -> {
                val end = now.plusHours(oneHourLong)
                newState = ConnectionScreenState(
                    selectedServer,
                    SnoozeState(
                        active = true,
                        formatter.format(end)
                    ), usageState
                )
            }
            else -> {
                newState = ConnectionScreenState(selectedServer, SNOOZE_STATE_DEFAULT, usageState)
            }
        }
        viewModelScope.launch {
            _state.emit(newState)
        }
    }

    private fun getSelectedServer() = viewModelScope.launch {
        if (availableServers.isNotEmpty()) {
            selectedServer =
                availableServers.firstOrNull { it.key == regionsUseCase.getSelectedRegion() }
                    ?: availableServers.sortedBy { it.latency?.toInt() }.firstOrNull()
            selectedServer?.let {
                regionsUseCase.selectRegion(it.key)
            }
            _state.emit(ConnectionScreenState(selectedServer, SNOOZE_STATE_DEFAULT, usageState))
        }
    }
}