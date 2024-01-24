package com.kape.connection.ui.vm

import android.app.AlarmManager
import android.os.Build
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kape.connection.ConnectionPrefs
import com.kape.connection.domain.ClientStateDataSource
import com.kape.customization.prefs.CustomizationPrefs
import com.kape.dedicatedip.domain.RenewDipUseCase
import com.kape.dip.DipPrefs
import com.kape.portforwarding.data.model.PortForwardingStatus
import com.kape.router.EnterFlow
import com.kape.router.Exit
import com.kape.router.Router
import com.kape.settings.SettingsPrefs
import com.kape.settings.data.VpnProtocols
import com.kape.snooze.SnoozeHandler
import com.kape.ui.tiles.MAX_SERVERS
import com.kape.utils.vpnserver.VpnServer
import com.kape.vpnconnect.domain.ConnectionUseCase
import com.kape.vpnconnect.provider.UsageProvider
import com.kape.vpnregions.domain.GetVpnRegionsUseCase
import com.kape.vpnregions.domain.ReadVpnRegionsDetailsUseCase
import com.kape.vpnregions.domain.SetVpnRegionsUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent

class ConnectionViewModel(
    private val setVpnRegionsUseCase: SetVpnRegionsUseCase,
    private val getVpnRegionsUseCase: GetVpnRegionsUseCase,
    private val readVpnRegionsDetailsUseCase: ReadVpnRegionsDetailsUseCase,
    private val connectionUseCase: ConnectionUseCase,
    private val clientStateDataSource: ClientStateDataSource,
    private val router: Router,
    private val prefs: ConnectionPrefs,
    private val settingsPrefs: SettingsPrefs,
    private val snoozeHandler: SnoozeHandler,
    private val usageProvider: UsageProvider,
    private val dipPrefs: DipPrefs,
    private val renewDipUseCase: RenewDipUseCase,
    private val customizationPrefs: CustomizationPrefs,
    private val alarmManager: AlarmManager,
) : ViewModel(), KoinComponent {

    private var availableVpnServers = mutableListOf<VpnServer>()
    var selectedVpnServer = mutableStateOf(prefs.getSelectedServer())
    private var lastSelectedVpnServerKey: String? = null
    val quickConnectVpnServers = mutableStateOf(emptyList<VpnServer>())
    val favoriteVpnServers = mutableStateOf(emptyList<VpnServer>())

    var ip by mutableStateOf(prefs.getClientIp())
    var vpnIp by mutableStateOf(prefs.getClientVpnIp())

    val download = usageProvider.download
    val upload = usageProvider.upload

    val portForwardingStatus = connectionUseCase.portForwardingStatus
    val port = connectionUseCase.port
    val isSnoozeActive = snoozeHandler.isSnoozeActive
    val timeUntilResume = snoozeHandler.timeUntilResume

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

    fun navigateToCustomization() {
        router.handleFlow(EnterFlow.Customization)
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

    fun getOrderedElements() = customizationPrefs.getOrderedElements()

    fun snooze(interval: Int) = snoozeHandler.setSnooze(interval)

    fun isAlarmPermissionGranted() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        alarmManager.canScheduleExactAlarms()
    } else {
        true
    }

    private fun vpnServersLoaded(vpnServers: List<VpnServer>) {
        availableVpnServers.clear()
        availableVpnServers.addAll(vpnServers)
        filterFavoriteServers()
        getQuickConnectServers()
        getSelectedServer()
        setVpnRegionsUseCase.setVpnServers(servers = vpnServers)
    }

    private fun renewDedicatedIps() = viewModelScope.launch {
        if (dipPrefs.getDedicatedIps().isNotEmpty()) {
            for (dip in dipPrefs.getDedicatedIps()) {
                renewDipUseCase.renew(dip.dipToken).collect()
            }
        }
    }

    private fun getSelectedServer() = viewModelScope.launch {
        if (availableVpnServers.isNotEmpty()) {
            selectedVpnServer.value =
                availableVpnServers.firstOrNull { it.key == getVpnRegionsUseCase.getSelectedVpnServerKey() }
                    ?: availableVpnServers.sortedBy { it.latency?.toInt() }.firstOrNull()
            selectedVpnServer.value?.let {
                getVpnRegionsUseCase.selectVpnServer(it.key)
                prefs.setSelectedServer(it)
            }
        }

        if (lastSelectedVpnServerKey == null) {
            lastSelectedVpnServerKey = selectedVpnServer.value?.key.toString()
        } else if (lastSelectedVpnServerKey != selectedVpnServer.value?.key) {
            selectedVpnServer.value?.let {
                lastSelectedVpnServerKey = selectedVpnServer.value?.key.toString()
                connectionUseCase.reconnect(it).collect()
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

    fun onSnoozeResumed() {
        connect()
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
        viewModelScope.launch {
            selectedVpnServer.value?.let {
                connectionUseCase.reconnect(it).collect {}
            }
        }
    }

    fun isPortForwardingEnabled() = settingsPrefs.isPortForwardingEnabled()

    private fun connect() {
        viewModelScope.launch {
            selectedVpnServer.value?.let {
                getVpnRegionsUseCase.selectVpnServer(it.key)
                prefs.addToQuickConnect(it.key)
                snoozeHandler.cancelSnooze()
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
        if (settingsPrefs.isAutomationEnabled()) {
            prefs.disconnectedByUser(true)
        }
        connectionUseCase.stopConnection().collect {
            clientStateDataSource.resetVpnIp()
            vpnIp = prefs.getClientVpnIp()
            portForwardingStatus.value = PortForwardingStatus.NoPortForwarding
        }
    }
}