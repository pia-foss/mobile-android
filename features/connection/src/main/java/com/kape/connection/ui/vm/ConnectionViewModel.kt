package com.kape.connection.ui.vm

import android.app.AlarmManager
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kape.connection.ConnectionPrefs
import com.kape.connection.utils.ConnectionScreenState
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
import com.kape.shadowsocksregions.domain.SetShadowsocksRegionsUseCase
import com.kape.snooze.SnoozeHandler
import com.kape.ui.mobile.tiles.MAX_SERVERS
import com.kape.utils.AUTO_KEY
import com.kape.utils.NetworkConnectionListener
import com.kape.utils.shadowsocksserver.ShadowsocksServer
import com.kape.utils.vpnserver.VpnServer
import com.kape.vpnconnect.domain.ConnectionUseCase
import com.kape.vpnconnect.provider.UsageProvider
import com.kape.vpnregions.VpnRegionPrefs
import com.kape.vpnregions.utils.RegionListProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent

class ConnectionViewModel(
    private val regionListProvider: RegionListProvider,
    private val setShadowsocksRegionsUseCase: SetShadowsocksRegionsUseCase,
    private val getShadowsocksRegionsUseCase: GetShadowsocksRegionsUseCase,
    private val connectionUseCase: ConnectionUseCase,
    private val router: Router,
    private val prefs: ConnectionPrefs,
    private val settingsPrefs: SettingsPrefs,
    private val snoozeHandler: SnoozeHandler,
    private val usageProvider: UsageProvider,
    private val dipPrefs: DipPrefs,
    private val renewDipUseCase: RenewDipUseCase,
    private val customizationPrefs: CustomizationPrefs,
    private val vpnRegionPrefs: VpnRegionPrefs,
    private val alarmManager: AlarmManager,
    networkConnectionListener: NetworkConnectionListener,
) : ViewModel(), KoinComponent {

    private val defaultState = ConnectionScreenState(
        server = prefs.getSelectedVpnServer() ?: regionListProvider.servers.value.first(),
        quickConnectServers = getQuickConnectVpnServers(),
        isCurrentServerOptimal = false,
        showOptimalLocationInfo = prefs.getSelectedVpnServer() == null,
    )

    private val _state: MutableStateFlow<ConnectionScreenState> = MutableStateFlow(defaultState)
    val state: StateFlow<ConnectionScreenState> = _state

    val isConnected = networkConnectionListener.isConnected

    val clientIp = connectionUseCase.clientIp
    val vpnIp = connectionUseCase.vpnIp

    val download = usageProvider.download
    val upload = usageProvider.upload

    val portForwardingStatus = connectionUseCase.portForwardingStatus
    val port = connectionUseCase.port
    val isSnoozeActive = snoozeHandler.isSnoozeActive
    val timeUntilResume = snoozeHandler.timeUntilResume

    init {
        viewModelScope.launch {
            connectionUseCase.getClientStatus().collect()
        }
        renewDedicatedIps()
    }

    fun navigateToSettings() {
        router.handleFlow(EnterFlow.Settings)
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
        regionListProvider.updateServerList(locale, isConnectionActive()).collect {
            prefs.getSelectedVpnServer()?.let {
                updateState(it, false)
            } ?: run {
                if (!connectionUseCase.isConnected() || !connectionUseCase.isConnecting()) {
                    updateState(
                        regionListProvider.servers.value.sortedBy { it.latency?.toInt() }
                            .first(),
                        false,
                    )
                }
            }
        }
    }

    fun loadShadowsocksServers(locale: String) = viewModelScope.launch {
        getShadowsocksRegionsUseCase.fetchShadowsocksServers(locale).collect {
            setShadowsocksRegionsUseCase.setShadowsocksServers(shadowsocksServers = it)
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

    private fun renewDedicatedIps() = viewModelScope.launch {
        if (dipPrefs.getDedicatedIps().isNotEmpty()) {
            for (dip in dipPrefs.getDedicatedIps()) {
                renewDipUseCase.renew(dip.dipToken).collect()
            }
        }
    }

    fun getSelectedShadowsocksServer(): ShadowsocksServer =
        getShadowsocksRegionsUseCase.getSelectedShadowsocksServer()

    private fun getFavoriteServers(): List<VpnServer> {
        val favoriteServers = mutableListOf<VpnServer>()
        for (item in vpnRegionPrefs.getFavoriteVpnServers()) {
            regionListProvider.servers.value.firstOrNull { it.name == item.name && it.isDedicatedIp == item.isDip }
                ?.let {
                    favoriteServers.add(it)
                }
        }
        return favoriteServers
    }

    private fun getQuickConnectVpnServers(): List<VpnServer> {
        val orderedServers = mutableListOf<VpnServer>()
        if (getFavoriteServers().size > MAX_SERVERS) {
            for (index in 0 until MAX_SERVERS) {
                orderedServers.add(getFavoriteServers()[index])
            }
        } else {
            for (index in 0 until getFavoriteServers().size) {
                orderedServers.add(getFavoriteServers()[index])
            }
            val previousConnections = prefs.getQuickConnectServers().reversed()
            for (server in previousConnections) {
                regionListProvider.servers.value.firstOrNull { it.key == server }?.let {
                    if (orderedServers.firstOrNull { it.key == server } == null) {
                        orderedServers.add(it)
                    }
                }
            }
        }
        return orderedServers
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
        if (connectionUseCase.isConnected() || connectionUseCase.isConnecting()) {
            disconnect()
        } else {
            connect()
        }
    }

    fun getConnectionSettings() = when (settingsPrefs.getSelectedProtocol()) {
        VpnProtocols.WireGuard -> settingsPrefs.getWireGuardSettings()
        VpnProtocols.OpenVPN -> settingsPrefs.getOpenVpnSettings()
    }

    fun quickConnect(server: VpnServer) {
        vpnRegionPrefs.selectVpnServer(server)
        updateState(state.value.server, false)
    }

    fun isPortForwardingEnabled() = settingsPrefs.isPortForwardingEnabled()

    fun isVpnServerFavorite(serverName: String, isDip: Boolean): Boolean {
        return vpnRegionPrefs.isFavorite(serverName, isDip)
    }

    private fun connect() = viewModelScope.launch {
        prefs.setSelectedVpnServer(state.value.server)
        prefs.addToQuickConnect(state.value.server.key)
        updateState(state.value.server, false)
        snoozeHandler.cancelSnooze()
        connectionUseCase.startConnection(
            server = state.value.server,
            isManualConnection = true,
        ).cancellable().collect()
    }

    private fun disconnect() = viewModelScope.launch {
        if (settingsPrefs.isAutomationEnabled()) {
            prefs.disconnectedByUser(true)
        }
        connectionUseCase.stopConnection().collect {
            portForwardingStatus.value = PortForwardingStatus.NoPortForwarding
        }
    }

    private fun isOptimalLocation(serverKey: String): Boolean {
        return regionListProvider.servers.value.sortedBy { it.latency?.toInt() }
            .first().key == serverKey
    }

    private fun updateState(server: VpnServer, showOptimalLocationInfo: Boolean) =
        viewModelScope.launch {
            val existingServer = vpnRegionPrefs.getSelectedServer()
            existingServer?.let {
                if (server.key != existingServer.key) {
                    val serverToConnect =
                        if (existingServer.key == AUTO_KEY) {
                            regionListProvider.servers.value.sortedBy { it.latency?.toInt() }
                                .first()
                        } else {
                            it
                        }
                    if (vpnRegionPrefs.needsVpnReconnect()) {
                        vpnRegionPrefs.setVpnReconnect(false)
                        prefs.setSelectedVpnServer(serverToConnect)
                        prefs.addToQuickConnect(serverToConnect.key)
                        _state.emit(
                            ConnectionScreenState(
                                serverToConnect,
                                getQuickConnectVpnServers(),
                                isOptimalLocation(serverToConnect.key),
                                showOptimalLocationInfo,
                            ),
                        )
                        connectionUseCase.reconnect(serverToConnect).collect()
                    }
                }
            } ?: run {
                _state.emit(
                    ConnectionScreenState(
                        server,
                        getQuickConnectVpnServers(),
                        isOptimalLocation(server.key),
                        showOptimalLocationInfo,
                    ),
                )
            }
        }
}