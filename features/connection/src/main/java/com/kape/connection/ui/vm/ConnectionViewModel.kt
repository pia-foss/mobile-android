package com.kape.connection.ui.vm

import android.app.AlarmManager
import android.os.Build
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kape.connection.ConnectionPrefs
import com.kape.connection.utils.ConnectionData
import com.kape.connection.utils.ConnectionScreenState
import com.kape.customization.data.Element
import com.kape.customization.data.ScreenElement
import com.kape.customization.prefs.CustomizationPrefs
import com.kape.dedicatedip.domain.RenewDipUseCase
import com.kape.dip.DipPrefs
import com.kape.portforwarding.data.model.PortForwardingStatus
import com.kape.rating.data.RatingDialogType
import com.kape.rating.utils.RatingTool
import com.kape.router.AutomationSettings
import com.kape.router.Customization
import com.kape.router.DedicatedIpSignupPlans
import com.kape.router.HelpSettings
import com.kape.router.KillSwitchSettings
import com.kape.router.ProtocolSettings
import com.kape.router.Router
import com.kape.router.Settings
import com.kape.router.ShadowsocksRegionSelection
import com.kape.router.TvSideMenu
import com.kape.router.VpnRegionSelection
import com.kape.settings.SettingsPrefs
import com.kape.settings.data.ObfuscationOptions
import com.kape.settings.data.VpnProtocols
import com.kape.shadowsocksregions.domain.GetShadowsocksRegionsUseCase
import com.kape.shadowsocksregions.domain.SetShadowsocksRegionsUseCase
import com.kape.shortcut.prefs.ShortcutPrefs
import com.kape.snooze.SnoozeHandler
import com.kape.ui.mobile.tiles.MAX_SERVERS
import com.kape.utils.AUTO_KEY
import com.kape.utils.NetworkConnectionListener
import com.kape.utils.shadowsocksserver.ShadowsocksServer
import com.kape.utils.vpnserver.VpnServer
import com.kape.vpnconnect.domain.ConnectionUseCase
import com.kape.vpnconnect.provider.UsageProvider
import com.kape.vpnconnect.utils.ConnectionStatus
import com.kape.vpnregions.VpnRegionPrefs
import com.kape.vpnregions.utils.RegionListProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent

@OptIn(ExperimentalCoroutinesApi::class)
class ConnectionViewModel(
    private val router: Router,
    private val regionListProvider: RegionListProvider,
    private val setShadowsocksRegionsUseCase: SetShadowsocksRegionsUseCase,
    private val getShadowsocksRegionsUseCase: GetShadowsocksRegionsUseCase,
    private val connectionUseCase: ConnectionUseCase,
    private val prefs: ConnectionPrefs,
    private val settingsPrefs: SettingsPrefs,
    private val snoozeHandler: SnoozeHandler,
    private val usageProvider: UsageProvider,
    private val dipPrefs: DipPrefs,
    private val renewDipUseCase: RenewDipUseCase,
    private val customizationPrefs: CustomizationPrefs,
    private val vpnRegionPrefs: VpnRegionPrefs,
    private val alarmManager: AlarmManager,
    private val ratingTool: RatingTool,
    private val shortcutPrefs: ShortcutPrefs,
    networkConnectionListener: NetworkConnectionListener,
) : ViewModel(), KoinComponent {

    private val defaultState = ConnectionScreenState(
        server = prefs.getSelectedVpnServer() ?: regionListProvider.getOptimalServer(),
        quickConnectServers = getQuickConnectVpnServers(),
        isCurrentServerOptimal = false,
        showOptimalLocationInfo = prefs.getSelectedVpnServer() == null,
        ratingDialogType = ratingTool.showRating.value,
        connectionData = ConnectionData(
            connectionUseCase.clientIp.value,
            connectionUseCase.vpnIp.value,
            connectionUseCase.getConnectionStatus().value,
            connectionUseCase.portForwardingStatus.value,
            connectionUseCase.port.value,
        ),
    )
    private val _state: MutableStateFlow<ConnectionScreenState> = MutableStateFlow(defaultState)
    val state: StateFlow<ConnectionScreenState> = _state
    val isConnected = networkConnectionListener.isConnected
    val download = usageProvider.download
    val upload = usageProvider.upload
    val isSnoozeActive = snoozeHandler.isSnoozeActive
    val timeUntilResume = snoozeHandler.timeUntilResume
    val showDedicatedIpHomeBanner = mutableStateOf(false)

    init {
        viewModelScope.launch {
            combine(
                connectionUseCase.getConnectionStatus(),
                connectionUseCase.clientIp,
                connectionUseCase.vpnIp,
                connectionUseCase.portForwardingStatus,
                connectionUseCase.port,
            ) { status, clientIp, vpnIp, pfwdStatus, port ->
                _state.update {
                    it.copy(
                        connectionData = ConnectionData(
                            clientIp,
                            vpnIp,
                            status,
                            pfwdStatus,
                            port,
                        ),
                    )
                }
                ConnectionInfoStatus(status, clientIp, vpnIp)
            }.flatMapLatest {
                when (it.status) {
                    ConnectionStatus.CONNECTED,
                    ConnectionStatus.DISCONNECTED,
                        -> connectionUseCase.getClientStatus(it.status)

                    else -> {
                        connectionUseCase.resetVpnIp()
                    }
                }
            }.collect()
        }
        ratingTool.start()
        renewDedicatedIps()
        if (shortcutPrefs.isShortcutSettings()) {
            shortcutPrefs.setShortcutSettings(false)
            router.updateDestination(Settings)
        }
        if (shortcutPrefs.isShortcutChangeServer()) {
            shortcutPrefs.setShortcutChangeServer(false)
            showVpnRegionSelection()
        }
    }

    fun navigateToHelp() = router.updateDestination(HelpSettings)

    fun navigateToSideMenu() {
        router.updateDestination(TvSideMenu)
    }

    fun navigateToKillSwitch() {
        router.updateDestination(KillSwitchSettings)
    }

    fun navigateToAutomation() {
        router.updateDestination(AutomationSettings)
    }

    fun navigateToProtocols() {
        router.updateDestination(ProtocolSettings)
    }

    fun navigateToCustomization() {
        router.updateDestination(Customization)
    }

    fun navigateToDedicatedIpPlans() {
        router.updateDestination(DedicatedIpSignupPlans)
    }

    fun autoConnect() {
        viewModelScope.launch {
            if (settingsPrefs.isConnectOnLaunchEnabled() || shortcutPrefs.isShortcutConnectToVpn()) {
                shortcutPrefs.setShortcutConnectToVpn(false)
                prefs.getSelectedVpnServer()?.let {
                    connectionUseCase.startConnection(it, false).collect()
                } ?: run {
                    connectionUseCase.startConnection(
                        regionListProvider.getOptimalServer(),
                        false,
                    )
                        .collect()
                }
            }
        }
    }

    fun isConnectionActive() = connectionUseCase.isConnected()

    fun loadVpnServers(locale: String) = viewModelScope.launch {
        regionListProvider.updateServerLatencies(isConnectionActive(), false).collect {
            prefs.getSelectedVpnServer()?.let {
                updateState(it, false)
            } ?: run {
                if (!connectionUseCase.isConnected() || !connectionUseCase.isConnecting()) {
                    updateState(
                        regionListProvider.getOptimalServer(),
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

    fun shouldShowDedicatedIpSignupBanner() {
        if (dipPrefs.isDipSignupEnabled() && dipPrefs.showDedicatedIpHomeBanner()) {
            showDedicatedIpHomeBanner.value = true
        }
    }

    fun hideDedicatedIpSignupBanner() {
        dipPrefs.hideDedicatedIpHomeBanner()
        showDedicatedIpHomeBanner.value = false
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
                regionListProvider.servers.value.firstOrNull { it.key == server.serverKey && it.isDedicatedIp == server.isDip }
                    ?.let {
                        if (orderedServers.firstOrNull { it.key == server.serverKey && it.isDedicatedIp == server.isDip } == null) {
                            orderedServers.add(it)
                        }
                    }
            }
        }
        return orderedServers
    }

    fun showVpnRegionSelection() {
        router.updateDestination(VpnRegionSelection)
    }

    fun showShadowsocksRegionSelection() {
        router.updateDestination(ShadowsocksRegionSelection)
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
        connect()
    }

    fun isPortForwardingEnabled() = settingsPrefs.isPortForwardingEnabled()

    fun isVpnServerFavorite(serverName: String, isDip: Boolean): Boolean {
        return vpnRegionPrefs.isFavorite(serverName, isDip)
    }

    private fun connect() = viewModelScope.launch {
        prefs.setSelectedVpnServer(state.value.server)
        prefs.addToQuickConnect(state.value.server.key, state.value.server.isDedicatedIp)
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
        connectionUseCase.stopConnection().collect()
    }

    private fun isOptimalLocation(serverKey: String): Boolean {
        return regionListProvider.getOptimalServer().key == serverKey
    }

    private fun updateState(server: VpnServer, showOptimalLocationInfo: Boolean) =
        viewModelScope.launch {
            val existingServer = vpnRegionPrefs.getSelectedServer()
            existingServer?.let {
                if (server != existingServer) {
                    val serverToConnect =
                        if (existingServer.key == AUTO_KEY) {
                            regionListProvider.getOptimalServer()
                        } else {
                            it
                        }
                    if (vpnRegionPrefs.needsVpnReconnect()) {
                        vpnRegionPrefs.setVpnReconnect(false)
                        prefs.setSelectedVpnServer(serverToConnect)
                        prefs.addToQuickConnect(
                            serverToConnect.key,
                            serverToConnect.isDedicatedIp,
                        )
                        _state.update {
                            it.copy(
                                server = serverToConnect,
                                quickConnectServers = getQuickConnectVpnServers(),
                                isCurrentServerOptimal = isOptimalLocation(serverToConnect.key),
                            )
                        }
                        connectionUseCase.reconnect(serverToConnect).collect()
                    }
                } else {
                    if (!connectionUseCase.isConnected() && vpnRegionPrefs.needsVpnReconnect()) {
                        vpnRegionPrefs.setVpnReconnect(false)
                        prefs.addToQuickConnect(server.key, server.isDedicatedIp)
                        connectionUseCase.startConnection(server, true).collect { }
                        _state.update {
                            it.copy(
                                server = server,
                                quickConnectServers = getQuickConnectVpnServers(),
                                isCurrentServerOptimal = isOptimalLocation(server.key),
                            )
                        }
                    }
                }
            } ?: run {
                _state.update {
                    it.copy(
                        server = server,
                        quickConnectServers = getQuickConnectVpnServers(),
                        isCurrentServerOptimal = isOptimalLocation(server.key),
                        showOptimalLocationInfo = showOptimalLocationInfo,
                    )
                }
            }
        }

    fun showReviewPrompt() = _state.update { it.copy(ratingDialogType = RatingDialogType.Review) }

    fun showFeedbackPrompt() =
        _state.update { it.copy(ratingDialogType = RatingDialogType.Feedback) }

    fun setRatingStateInactive() {
        ratingTool.setRatingInactive()
        _state.update { it.copy(ratingDialogType = null) }
    }

    fun updateRatingDate() {
        ratingTool.updateRatingDate()
        _state.update { it.copy(ratingDialogType = null) }
    }

    fun refreshState() =
        _state.update { it.copy(quickConnectServers = getQuickConnectVpnServers()) }

    private data class ConnectionInfoStatus(
        val status: ConnectionStatus,
        val clientIp: String,
        val vpnIp: String,
    )
}