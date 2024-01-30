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
import com.kape.customization.data.Element
import com.kape.customization.data.ScreenElement
import com.kape.customization.prefs.CustomizationPrefs
import com.kape.dedicatedip.domain.RenewDipUseCase
import com.kape.dip.DipPrefs
import com.kape.portforwarding.data.model.PortForwardingStatus
import com.kape.router.EnterFlow
import com.kape.router.Exit
import com.kape.router.Router
import com.kape.settings.SettingsPrefs
import com.kape.settings.data.ObfuscationOptions
import com.kape.settings.data.VpnProtocols
import com.kape.shadowsocksregions.domain.GetShadowsocksRegionsUseCase
import com.kape.shadowsocksregions.domain.ReadShadowsocksRegionsDetailsUseCase
import com.kape.shadowsocksregions.domain.SetShadowsocksRegionsUseCase
import com.kape.snooze.SnoozeHandler
import com.kape.ui.tiles.MAX_SERVERS
import com.kape.utils.shadowsocksserver.ShadowsocksServer
import com.kape.utils.vpnserver.VpnServer
import com.kape.vpnconnect.domain.ConnectionUseCase
import com.kape.vpnconnect.provider.UsageProvider
import com.kape.vpnregions.domain.GetVpnRegionsUseCase
import com.kape.vpnregions.domain.ReadVpnRegionsDetailsUseCase
import com.kape.vpnregions.domain.SetVpnRegionsUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent

class ConnectionViewModel(
    private val setVpnRegionsUseCase: SetVpnRegionsUseCase,
    private val getVpnRegionsUseCase: GetVpnRegionsUseCase,
    private val readVpnRegionsDetailsUseCase: ReadVpnRegionsDetailsUseCase,
    private val setShadowsocksRegionsUseCase: SetShadowsocksRegionsUseCase,
    private val getShadowsocksRegionsUseCase: GetShadowsocksRegionsUseCase,
    private val readShadowsocksRegionsDetailsUseCase: ReadShadowsocksRegionsDetailsUseCase,
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
    var selectedVpnServer = mutableStateOf(prefs.getSelectedVpnServer())
    private var lastSelectedVpnServerKey: String? = null
    val quickConnectVpnServers = mutableStateOf(emptyList<VpnServer>())
    private val favoriteVpnServers = mutableStateOf(emptyList<VpnServer>())

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
                prefs.getSelectedVpnServer()?.let {
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

    fun loadShadowsocksServers(locale: String) = viewModelScope.launch {
        // If there are no servers persisted. Let's use the initial set of servers we are
        // shipping the application with while we perform a request for an updated version.
        if (getShadowsocksRegionsUseCase.getShadowsocksServers().isEmpty()) {
            val servers = readShadowsocksRegionsDetailsUseCase.readShadowsocksRegionsDetailsFromAssetsFolder()
            shadowsocksServersLoaded(shadowsocksServers = servers)
        }

        getShadowsocksRegionsUseCase.fetchShadowsocksServers(locale).collect {
            shadowsocksServersLoaded(shadowsocksServers = it)
        }
    }

    fun setIps() = viewModelScope.launch {
        clientStateDataSource.getClientStatus().collect { connected ->
            if (!connected) {
                ip = prefs.getClientIp()
                clientStateDataSource.resetVpnIp()
            }
            vpnIp = prefs.getClientVpnIp()
        }
    }

    fun getOrderedElements() = customizationPrefs.getOrderedElements()

    fun isScreenElementVisible(screenElement: ScreenElement): Boolean =
        when (screenElement.element) {
            Element.ShadowsocksRegionSelection ->
                screenElement.isVisible &&
                    settingsPrefs.isShadowsocksObfuscationEnabled() &&
                    settingsPrefs.getSelectedProtocol() == VpnProtocols.OpenVPN &&
                    settingsPrefs.getSelectedObfuscationOption() == ObfuscationOptions.PIA
            Element.VpnRegionSelection,
            Element.ConnectionInfo,
            Element.QuickConnect,
            Element.QuickSettings,
            Element.Snooze,
            Element.IpInfo,
            Element.Traffic,
            -> screenElement.isVisible
        }

    fun snooze(interval: Int) = snoozeHandler.setSnooze(interval)

    fun isAlarmPermissionGranted() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        alarmManager.canScheduleExactAlarms()
    } else {
        true
    }

    private fun vpnServersLoaded(vpnServers: List<VpnServer>) {
        availableVpnServers.clear()
        availableVpnServers.addAll(vpnServers)
        filterFavoriteVpnServers()
        getQuickConnectVpnServers()
        getSelectedVpnServer()
        setVpnRegionsUseCase.setVpnServers(servers = vpnServers)
    }

    private fun shadowsocksServersLoaded(shadowsocksServers: List<ShadowsocksServer>) {
        setShadowsocksRegionsUseCase.setShadowsocksServers(shadowsocksServers = shadowsocksServers)
    }

    private fun renewDedicatedIps() = viewModelScope.launch {
        if (dipPrefs.getDedicatedIps().isNotEmpty()) {
            for (dip in dipPrefs.getDedicatedIps()) {
                renewDipUseCase.renew(dip.dipToken).collect()
            }
        }
    }

    private fun getSelectedVpnServer() = viewModelScope.launch {
        if (availableVpnServers.isNotEmpty()) {
            selectedVpnServer.value =
                availableVpnServers.firstOrNull { it.key == getVpnRegionsUseCase.getSelectedVpnServerKey() }
                    ?: availableVpnServers.sortedBy { it.latency?.toInt() }.firstOrNull()
            selectedVpnServer.value?.let {
                getVpnRegionsUseCase.selectVpnServer(it.key)
                prefs.setSelectedVpnServer(it)
            }
        }

        if (lastSelectedVpnServerKey == null) {
            lastSelectedVpnServerKey = selectedVpnServer.value?.key.toString()
        } else if (lastSelectedVpnServerKey != selectedVpnServer.value?.key) {
            selectedVpnServer.value?.let {
                lastSelectedVpnServerKey = selectedVpnServer.value?.key.toString()
                reconnect()
            }
        }
    }

    fun getSelectedShadowsocksServer(): ShadowsocksServer? {
        val shadowsocksServers = getShadowsocksRegionsUseCase.getShadowsocksServers()
        if (shadowsocksServers.isEmpty()) {
            return null
        }

        return getShadowsocksRegionsUseCase.getSelectedShadowsocksServer()?.let { selectedShadowsocksServer ->
            shadowsocksServers.firstOrNull { shadowsocksServer ->
                shadowsocksServer.region == selectedShadowsocksServer.region
            }
        } ?: shadowsocksServers.first()
    }

    private fun filterFavoriteVpnServers() {
        favoriteVpnServers.value =
            availableVpnServers.filter { it.name in getVpnRegionsUseCase.getFavoriteVpnServers() }
    }

    private fun getQuickConnectVpnServers() {
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

    fun showVpnRegionSelection() {
        router.handleFlow(EnterFlow.VpnRegionSelection)
    }

    fun showShadowsocksRegionSelection() {
        router.handleFlow(EnterFlow.ShadowsocksRegionSelection)
    }

    fun onSnoozeResumed() {
        connect()
    }

    fun onConnectionButtonClicked() = viewModelScope.launch {
        if (connectionUseCase.isConnected()) {
            disconnect().collect() {}
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
                reconnect()
            }
        }
    }

    fun isPortForwardingEnabled() = settingsPrefs.isPortForwardingEnabled()

    private fun reconnect() = viewModelScope.launch {
        if (connectionUseCase.isConnected()) {
            disconnect().collect() {
                connect()
            }
        } else {
            connect()
        }
    }

    private fun connect() = viewModelScope.launch {
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
                    setIps()
                }
            }
        }
    }

    private fun disconnect(): Flow<Unit> = flow {
        if (settingsPrefs.isAutomationEnabled()) {
            prefs.disconnectedByUser(true)
        }
        connectionUseCase.stopConnection().collect {
            clientStateDataSource.resetVpnIp()
            vpnIp = prefs.getClientVpnIp()
            portForwardingStatus.value = PortForwardingStatus.NoPortForwarding
            emit(Unit)
        }
    }
}