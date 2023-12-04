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
import com.kape.connection.utils.SnoozeInterval
import com.kape.dedicatedip.domain.RenewDipUseCase
import com.kape.dip.DipPrefs
import com.kape.portforwarding.domain.PortForwardingUseCase
import com.kape.router.EnterFlow
import com.kape.router.Exit
import com.kape.router.Router
import com.kape.settings.SettingsPrefs
import com.kape.settings.data.VpnProtocols
import com.kape.utils.server.VpnServer
import com.kape.vpnconnect.domain.ConnectionUseCase
import com.kape.vpnconnect.provider.UsageProvider
import com.kape.vpnregions.domain.GetVpnRegionsUseCase
import com.kape.vpnregions.domain.ReadVpnRegionsDetailsUseCase
import com.kape.vpnregions.domain.SetVpnRegionsUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar

class ConnectionViewModel(
    private val setVpnRegionsUseCase: SetVpnRegionsUseCase,
    private val getVpnRegionsUseCase: GetVpnRegionsUseCase,
    private val readVpnRegionsDetailsUseCase: ReadVpnRegionsDetailsUseCase,
    private val connectionUseCase: ConnectionUseCase,
    private val clientStateDataSource: ClientStateDataSource,
    private val router: Router,
    private val prefs: ConnectionPrefs,
    private val settingsPrefs: SettingsPrefs,
    private val setSnoozePendingIntent: PendingIntent,
    private val usageProvider: UsageProvider,
    private val portForwardingUseCase: PortForwardingUseCase,
    private val dipPrefs: DipPrefs,
    private val renewDipUseCase: RenewDipUseCase,
) : ViewModel(), KoinComponent {

    private val oneHourLong = 1L
    private val fiveMinuteLong = 5L
    private val fifteenMinuteLong = 15L

    private var availableVpnServers = mutableListOf<VpnServer>()
    var selectedVpnServer = mutableStateOf(prefs.getSelectedServer())
    val snoozeActive = mutableStateOf(false)
    val favoriteVpnServers = mutableStateOf(emptyList<VpnServer>())
    val quickConnectVpnServers = mutableStateOf(emptyList<VpnServer>())
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
        renewDedicatedIps()
    }

    fun navigateToKillSwitch() {
        router.handleFlow(EnterFlow.KillSwitchSettings)
    }

    fun navigateToAutomation() {
        router.handleFlow(EnterFlow.AutomationSettings)
    }

    fun navigateToProtocols() {
        router.handleFlow(EnterFlow.ProtocolSettings)
    }

    fun exitApp() {
        router.handleFlow(Exit)
    }

    fun autoConnect() {
        viewModelScope.launch {
            if (settingsPrefs.isConnectOnLaunchEnabled()) {
                prefs.getSelectedServer()?.let {
                    connectionUseCase.startConnection(it, false).collect()
                }
            }
        }
    }

    fun isConnectionActive() = connectionUseCase.isConnected()

    fun loadVpnServers(locale: String) = viewModelScope.launch {
        // If there are no servers persisted. Let's use the initial set of servers we are
        // shipping the application with while we perform a request for an updated version.
        if (getVpnRegionsUseCase.getVpnServers().isEmpty()) {
            val servers = readVpnRegionsDetailsUseCase.readVpnRegionsDetailsFromAssetsFolder()
            vpnServersLoaded(vpnServers = servers)
        }

        getVpnRegionsUseCase.loadVpnServers(locale).collect {
            vpnServersLoaded(vpnServers = it)
        }
    }

    private fun vpnServersLoaded(vpnServers: List<VpnServer>) {
        availableVpnServers.clear()
        availableVpnServers.addAll(vpnServers)
        filterFavoriteServers()
        getQuickConnectServers()
        getSelectedServer()
        setVpnRegionsUseCase.setVpnServers(servers = vpnServers)
    }

    fun snooze(context: Context, interval: SnoozeInterval) {
        val formatter = DateTimeFormatter.ofPattern("HH:mm")
        val nowInMillis = Calendar.getInstance().timeInMillis
        val nowTime = LocalTime.now()
        val end: Long
        when (interval) {
            SnoozeInterval.SNOOZE_SHORT_MS -> {
                end = nowInMillis + SnoozeInterval.SNOOZE_SHORT_MS.value
                snoozeActive.value = true
            }

            SnoozeInterval.SNOOZE_MEDIUM_MS -> {
                end = nowInMillis + SnoozeInterval.SNOOZE_MEDIUM_MS.value
                snoozeActive.value = true
            }

            SnoozeInterval.SNOOZE_LONG_MS -> {
                end = nowInMillis + SnoozeInterval.SNOOZE_LONG_MS.value
                snoozeActive.value = true
            }

            else -> {
                end = 0
                snoozeActive.value = false
            }
        }
        if (snoozeActive.value) {
            disconnect()
            setSnoozeAlarm(context, end)
        }
    }

    private fun renewDedicatedIps() = viewModelScope.launch {
        if (dipPrefs.getDedicatedIps().isNotEmpty()) {
            for (dip in dipPrefs.getDedicatedIps()) {
                renewDipUseCase.renew(dip.dipToken).collect()
            }
        }
    }

    private fun getSelectedServer() {
        if (availableVpnServers.isNotEmpty()) {
            selectedVpnServer.value =
                availableVpnServers.firstOrNull { it.key == getVpnRegionsUseCase.getSelectedVpnServerKey() }
                    ?: availableVpnServers.sortedBy { it.latency?.toInt() }.firstOrNull()
            selectedVpnServer.value?.let {
                getVpnRegionsUseCase.selectVpnServer(it.key)
                prefs.setSelectedServer(it)
            }
        }
    }

    private fun filterFavoriteServers() {
        favoriteVpnServers.value =
            availableVpnServers.filter { it.name in getVpnRegionsUseCase.getFavoriteVpnServers() }
    }

    private fun getQuickConnectServers() {
        val servers = mutableListOf<String>()
        if (favoriteVpnServers.value.size > MAX_SERVERS) {
            for (index in MAX_SERVERS until favoriteVpnServers.value.size) {
                servers.add(favoriteVpnServers.value[index].key)
            }
        }
        val previousConnections =
            availableVpnServers.filter { it.key in prefs.getQuickConnectServers() }

        for (server in previousConnections) {
            servers.add(server.key)
        }
        quickConnectVpnServers.value = availableVpnServers.filter { it.key in servers }
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
        selectedVpnServer.value = availableVpnServers.firstOrNull { it.key == key }
        connect()
    }

    fun isPortForwardingEnabled() = settingsPrefs.isPortForwardingEnabled()

    private fun connect() {
        viewModelScope.launch {
            selectedVpnServer.value?.let {
                getVpnRegionsUseCase.selectVpnServer(it.key)
                prefs.addToQuickConnect(it.key)
                connectionUseCase.startConnection(
                    server = it,
                    isManualConnection = true,
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