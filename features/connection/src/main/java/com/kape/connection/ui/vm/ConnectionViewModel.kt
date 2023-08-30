package com.kape.connection.ui.vm

import android.app.Notification
import android.app.PendingIntent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kape.connection.ConnectionPrefs
import com.kape.connection.NO_IP
import com.kape.connection.domain.ClientStateDataSource
import com.kape.connection.ui.tiles.MAX_SERVERS
import com.kape.connection.utils.ConnectionScreenState
import com.kape.connection.utils.IDLE
import com.kape.connection.utils.SNOOZE_STATE_DEFAULT
import com.kape.connection.utils.SnoozeState
import com.kape.connection.utils.USAGE_STATE_DEFAULT
import com.kape.connection.utils.UsageState
import com.kape.regionselection.domain.GetRegionsUseCase
import com.kape.regionselection.domain.UpdateLatencyUseCase
import com.kape.router.EnterFlow
import com.kape.router.Router
import com.kape.utils.server.Server
import com.kape.vpnconnect.domain.ConnectionUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import java.time.LocalTime
import java.time.format.DateTimeFormatter

// TODO: temporary solution, will expand
class ConnectionViewModel(
    private val regionsUseCase: GetRegionsUseCase,
    private val updateLatencyUseCase: UpdateLatencyUseCase,
    private val connectionUseCase: ConnectionUseCase,
    private val clientStateDataSource: ClientStateDataSource,
    private val router: Router,
    private val prefs: ConnectionPrefs,
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

    private var availableServers = mutableListOf<Server>()
    private var selectedServer: Server? = null
    private var usageState: UsageState = USAGE_STATE_DEFAULT
    private var snoozeState: SnoozeState = SNOOZE_STATE_DEFAULT
    private var favoriteServers: List<Server> = emptyList()
    private var quickConnectServers: List<Server> = emptyList()

    var ip by mutableStateOf(prefs.getClientIp())
    var vpnIp by mutableStateOf(prefs.getClientVpnIp())

    init {
        viewModelScope.launch {
            clientStateDataSource.getClientStatus().collect()
        }
    }

    fun loadServers(locale: String) = viewModelScope.launch {
        regionsUseCase.loadRegions(locale).collect {
            availableServers.clear()
            updateLatencyUseCase.updateLatencies().collect {
                availableServers.addAll(it)
                filterFavoriteServers()
                getQuickConnectServers()
                getSelectedServer()
                selectedServer?.let { server ->
                    connectToServer(server)
                }
                _state.emit(
                    ConnectionScreenState(
                        selectedServer,
                        snoozeState,
                        usageState,
                        favoriteServers,
                        quickConnectServers,
                    ),
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
                    formatter.format(end),
                )
            }

            SNOOZE_MEDIUM_MS -> {
                val end = now.plusMinutes(fifteenMinuteLong)
                snoozeState = SnoozeState(
                    active = true,
                    formatter.format(end),
                )
            }

            SNOOZE_LONG_MS -> {
                val end = now.plusHours(oneHourLong)
                snoozeState = SnoozeState(
                    active = true,
                    formatter.format(end),
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
                    favoriteServers,
                    quickConnectServers,
                ),
            )
        }
    }

    private fun connectToServer(server: Server) {
        selectedServer = server
        regionsUseCase.selectRegion(server.key)
        // TODO: init connection
        prefs.addToQuickConnect(server.key)
        viewModelScope.launch {
            _state.emit(
                ConnectionScreenState(
                    selectedServer,
                    snoozeState,
                    usageState,
                    favoriteServers,
                    quickConnectServers,
                ),
            )
        }
    }

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

    private fun getQuickConnectServers() {
        val servers = mutableListOf<String>()
        if (favoriteServers.size > MAX_SERVERS) {
            for (index in MAX_SERVERS until favoriteServers.size) {
                servers.add(favoriteServers[index].key)
            }
        }
        val previousConnections =
            availableServers.filter { it.key in prefs.getQuickConnectServers() }

        for (server in previousConnections) {
            servers.add(server.key)
        }
        quickConnectServers = availableServers.filter { it.key in servers }
    }

    fun showRegionSelection() {
        router.handleFlow(EnterFlow.RegionSelection)
    }

    fun onConnectionButtonClicked(notification: Notification, pendingIntent: PendingIntent) {
        if (connectionUseCase.isConnected()) {
            disconnect()
        } else {
            connect(notification, pendingIntent)
        }
    }

    private fun connect(notification: Notification, pendingIntent: PendingIntent) {
        viewModelScope.launch {
            selectedServer?.let {
                connectionUseCase.startConnection(
                    it,
                    pendingIntent,
                    notification,
                ).collect {
                    launch {
                        delay(3000)
                        clientStateDataSource.getClientStatus().collect { connected ->
                            if (connected) {
                                vpnIp = prefs.getClientVpnIp()
                            } else {
                                ip = prefs.getClientIp()
                                clientStateDataSource.resetVpnIp()
                                vpnIp = prefs.getClientVpnIp()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun disconnect() = viewModelScope.launch {
        connectionUseCase.stopConnection().collect {
            clientStateDataSource.resetVpnIp()
            vpnIp = prefs.getClientVpnIp()
        }
    }
}