package com.kape.connection.ui.vm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kape.connection.ConnectionPrefs
import com.kape.connection.domain.ClientStateDataSource
import com.kape.connection.ui.tiles.MAX_SERVERS
import com.kape.connection.utils.SNOOZE_STATE_DEFAULT
import com.kape.connection.utils.SnoozeInterval
import com.kape.connection.utils.SnoozeState
import com.kape.portforwarding.domain.PortForwardingUseCase
import com.kape.regionselection.domain.GetRegionsUseCase
import com.kape.regionselection.domain.UpdateLatencyUseCase
import com.kape.router.EnterFlow
import com.kape.router.Router
import com.kape.settings.SettingsPrefs
import com.kape.settings.data.VpnProtocols
import com.kape.utils.server.Server
import com.kape.vpnconnect.domain.ConnectionUseCase
import com.kape.vpnconnect.provider.UsageProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar

class ConnectionViewModel(
    private val regionsUseCase: GetRegionsUseCase,
    private val updateLatencyUseCase: UpdateLatencyUseCase,
    private val connectionUseCase: ConnectionUseCase,
    private val clientStateDataSource: ClientStateDataSource,
    private val router: Router,
    private val prefs: ConnectionPrefs,
    private val settingsPrefs: SettingsPrefs,
    private val setSnoozePendingIntent: PendingIntent,
    private val usageProvider: UsageProvider,
    private val portForwardingUseCase: PortForwardingUseCase,
) : ViewModel(), KoinComponent {

    private val oneHourLong = 1L
    private val fiveMinuteLong = 5L
    private val fifteenMinuteLong = 15L

    private var availableServers = mutableListOf<Server>()
    var selectedServer = mutableStateOf(prefs.getSelectedServer())
    val snoozeState = mutableStateOf(SNOOZE_STATE_DEFAULT)
    val favoriteServers = mutableStateOf(emptyList<Server>())
    val quickConnectServers = mutableStateOf(emptyList<Server>())
    val snoozeTime = mutableLongStateOf(prefs.getLastSnoozeEndTime())

    var ip by mutableStateOf(prefs.getClientIp())
    var vpnIp by mutableStateOf(prefs.getClientVpnIp())

    val download = usageProvider.download
    val upload = usageProvider.upload

    val portForwardingStatus = portForwardingUseCase.portForwardingStatus
    val port = mutableStateOf(prefs.getPortBindingInfo()?.decodedPayload?.port)

    init {
        viewModelScope.launch {
            clientStateDataSource.getClientStatus().collect()
        }
    }

    fun navigateToKillSwitch() {
        router.handleFlow(EnterFlow.KillSwitchSettings)
    }

    fun navigateToAutomation() {
        router.handleFlow(EnterFlow.AutomationSettings)
    }

    fun navigateToQuickSettings() {
        router.handleFlow(EnterFlow.QuickSettings)
    }

    fun autoConnect() {
        viewModelScope.launch {
            if (settingsPrefs.isConnectOnLaunchEnabled()) {
                prefs.getSelectedServer()?.let {
                    connectionUseCase.startConnection(it).collect()
                }
            }
        }
    }

    fun isConnectionActive() = connectionUseCase.isConnected()

    fun loadServers(locale: String) = viewModelScope.launch {
        regionsUseCase.loadRegions(locale).collect {
            availableServers.clear()
            updateLatencyUseCase.updateLatencies().collect {
                availableServers.addAll(it)
                filterFavoriteServers()
                getQuickConnectServers()
                getSelectedServer()
            }
        }
    }

    fun snooze(context: Context, interval: SnoozeInterval) {
        val formatter = DateTimeFormatter.ofPattern("HH:mm")
        val nowInMillis = Calendar.getInstance().timeInMillis
        val nowTime = LocalTime.now()
        val end: Long
        when (interval) {
            SnoozeInterval.SNOOZE_SHORT_MS -> {
                end = nowInMillis + SnoozeInterval.SNOOZE_SHORT_MS.value
                snoozeState.value = SnoozeState(
                    active = true,
                    formatter.format(nowTime.plusMinutes(fiveMinuteLong)),
                )
            }

            SnoozeInterval.SNOOZE_MEDIUM_MS -> {
                end = nowInMillis + SnoozeInterval.SNOOZE_MEDIUM_MS.value
                snoozeState.value = SnoozeState(
                    active = true,
                    formatter.format(nowTime.plusMinutes(fifteenMinuteLong)),
                )
            }

            SnoozeInterval.SNOOZE_LONG_MS -> {
                end = nowInMillis + SnoozeInterval.SNOOZE_LONG_MS.value
                snoozeState.value = SnoozeState(
                    active = true,
                    formatter.format(nowTime.plusHours(oneHourLong)),
                )
            }

            else -> {
                end = 0
                snoozeState.value = SNOOZE_STATE_DEFAULT
            }
        }
        if (snoozeState.value.active) {
            disconnect()
            setSnoozeAlarm(context, end)
        }
    }

    private fun getSelectedServer() {
        if (availableServers.isNotEmpty()) {
            selectedServer.value =
                availableServers.firstOrNull { it.key == regionsUseCase.getSelectedRegion() }
                    ?: availableServers.sortedBy { it.latency?.toInt() }.firstOrNull()
            selectedServer.value?.let {
                regionsUseCase.selectRegion(it.key)
            }
        }
    }

    private fun filterFavoriteServers() {
        favoriteServers.value =
            availableServers.filter { it.name in regionsUseCase.getFavoriteServers() }
    }

    private fun getQuickConnectServers() {
        val servers = mutableListOf<String>()
        if (favoriteServers.value.size > MAX_SERVERS) {
            for (index in MAX_SERVERS until favoriteServers.value.size) {
                servers.add(favoriteServers.value[index].key)
            }
        }
        val previousConnections =
            availableServers.filter { it.key in prefs.getQuickConnectServers() }

        for (server in previousConnections) {
            servers.add(server.key)
        }
        quickConnectServers.value = availableServers.filter { it.key in servers }
    }

    fun showRegionSelection() {
        router.handleFlow(EnterFlow.RegionSelection)
    }

    fun onConnectionButtonClicked() {
        if (connectionUseCase.isConnected()) {
            disconnect()
        } else {
            connect()
        }
    }

    fun getConnectionSettings() = when (settingsPrefs.getSelectedProtocol()) {
        VpnProtocols.WireGuard -> settingsPrefs.getWireGuardSettings()
        VpnProtocols.OpenVPN -> settingsPrefs.getOpenVpnSettings()
    }

    fun quickConnect(key: String) {
        selectedServer.value = availableServers.firstOrNull { it.key == key }
        connect()
    }

    fun isPortForwardingEnabled() = settingsPrefs.isPortForwardingEnabled()

    fun isKillSwitchEnabled() = settingsPrefs.isQuickSettingKillSwitchEnabled()

    fun isAutomationEnabled() = settingsPrefs.isQuickSettingAutomationEnabled()
    
    fun isPrivateBrowserEnabled() = settingsPrefs.isQuickSettingPrivateBrowserEnabled()

    private fun connect() {
        viewModelScope.launch {
            selectedServer.value?.let {
                regionsUseCase.selectRegion(it.key)
                prefs.addToQuickConnect(it.key)
                connectionUseCase.startConnection(
                    it,
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
            usageProvider.reset()
        }
    }

    private fun setSnoozeAlarm(context: Context, time: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent()
                intent.action = Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                context.startActivity(intent)
            } else {
                setSnooze(alarmManager, time)
            }
        } else {
            setSnooze(alarmManager, time)
        }
    }

    private fun setSnooze(alarmManager: AlarmManager, time: Long) {
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC, time, setSnoozePendingIntent)
        prefs.setLastSnoozeEndTime(time)
    }
}