package com.kape.connection.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kape.connection.utils.*
import com.kape.region_selection.domain.GetRegionsUseCase
import com.kape.region_selection.domain.UpdateLatencyUseCase
import com.kape.region_selection.server.Server
import com.kape.router.EnterFlow
import com.kape.router.Router
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.time.LocalTime
import java.time.format.DateTimeFormatter

// TODO: temporary solution, will expand
class ConnectionViewModel(
    private val regionsUseCase: GetRegionsUseCase,
    private val updateLatencyUseCase: UpdateLatencyUseCase,
) : ViewModel(), KoinComponent {

    private val oneHourLong = 1L
    private val fiveMinuteLong = 5L
    private val fifteenMinuteLong = 15L

    val SNOOZE_DEFAULT_MS = 0
    val SNOOZE_SHORT_MS = 5 * 60 * 1000
    val SNOOZE_MEDIUM_MS = 15 * 60 * 1000
    val SNOOZE_LONG_MS = 60 * 60 * 1000

    private val _state = MutableStateFlow(IDLE)
    val state: StateFlow<ConnectionScreenState> = _state

    private val router: Router by inject()

    private var availableServers = mutableListOf<Server>()
    private var selectedServer: Server? = null
    private var usageState: UsageState = USAGE_STATE_DEFAULT
    private var snoozeState: SnoozeState = SNOOZE_STATE_DEFAULT
    private var favoriteServers: List<Server> = emptyList()

    fun loadServers(locale: String) = viewModelScope.launch {
        regionsUseCase.loadRegions(locale).collect {
            availableServers.clear()
            updateLatencyUseCase.updateLatencies().collect {
                availableServers.addAll(it)
                filterFavoriteServers()
                getSelectedServer()
                _state.emit(
                    ConnectionScreenState(
                        selectedServer,
                        snoozeState,
                        usageState,
                        favoriteServers
                    )
                )
            }
        }
    }

    fun snooze(interval: Int) {
        val formatter = DateTimeFormatter.ofPattern("HH:mm")
        val now = LocalTime.now()
        // TODO: implement missing logic
        when (interval) {
            SNOOZE_SHORT_MS -> {
                val end = now.plusMinutes(fiveMinuteLong)
                snoozeState = SnoozeState(
                    active = true,
                    formatter.format(end)
                )
            }
            SNOOZE_MEDIUM_MS -> {
                val end = now.plusMinutes(fifteenMinuteLong)
                snoozeState = SnoozeState(
                    active = true,
                    formatter.format(end)
                )
            }
            SNOOZE_LONG_MS -> {
                val end = now.plusHours(oneHourLong)
                snoozeState = SnoozeState(
                    active = true,
                    formatter.format(end)
                )
            }
            else -> {
                snoozeState = SNOOZE_STATE_DEFAULT
            }
        }
        viewModelScope.launch {
            _state.emit(
                ConnectionScreenState(
                    selectedServer,
                    snoozeState,
                    usageState,
                    favoriteServers
                )
            )
        }
    }

    fun showSurvey() = router.handleFlow(EnterFlow.Survey)

    private fun getSelectedServer() {
        if (availableServers.isNotEmpty()) {
            selectedServer =
                availableServers.firstOrNull { it.key == regionsUseCase.getSelectedRegion() }
                    ?: availableServers.sortedBy { it.latency?.toInt() }.firstOrNull()
            selectedServer?.let {
                regionsUseCase.selectRegion(it.key)
            }
        }
    }

    private fun filterFavoriteServers() {
        favoriteServers =
            availableServers.filter { it.name in regionsUseCase.getFavoriteServers() }
    }

}