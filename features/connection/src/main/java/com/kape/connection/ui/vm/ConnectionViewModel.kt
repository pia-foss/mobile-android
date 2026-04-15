package com.kape.connection.ui.vm

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kape.buildconfig.data.BuildConfigProvider
import com.kape.connection.utils.ConnectionScreenState
import com.kape.contracts.ConnectionInfoProvider
import com.kape.contracts.ConnectionManager
import com.kape.contracts.Router
import com.kape.customization.data.Element
import com.kape.data.AutomationSettings
import com.kape.data.Customization
import com.kape.data.DedicatedIpSignupPlans
import com.kape.data.HelpSettings
import com.kape.data.KillSwitchSettings
import com.kape.data.ProtocolSettings
import com.kape.data.QUICK_CONNECT_MAX_SERVERS
import com.kape.data.Settings
import com.kape.data.ShadowsocksRegionSelection
import com.kape.data.TvSideMenu
import com.kape.data.VpnRegionSelection
import com.kape.data.vpnserver.VpnServer
import com.kape.dedicatedip.domain.RenewDipUseCase
import com.kape.localprefs.data.customization.ScreenElement
import com.kape.localprefs.prefs.ConnectionPrefs
import com.kape.localprefs.prefs.CustomizationPrefs
import com.kape.localprefs.prefs.DipPrefs
import com.kape.localprefs.prefs.SettingsPrefs
import com.kape.localprefs.prefs.ShortcutPrefs
import com.kape.localprefs.prefs.VpnRegionPrefs
import com.kape.rating.data.RatingDialogType
import com.kape.rating.utils.RatingTool
import com.kape.settings.data.ObfuscationOptions
import com.kape.settings.data.VpnProtocols
import com.kape.snooze.SnoozeHandler
import com.kape.utils.NetworkConnectionListener
import com.kape.vpnconnect.provider.UsageProvider
import com.kape.vpnregions.utils.RegionListProvider
import com.kape.vpnregions.utils.ShadowsocksListProvider
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class ConnectionViewModel(
    private val router: Router,
    private val regionListProvider: RegionListProvider,
    private val shadowsocksListProvider: ShadowsocksListProvider,
    private val connectionManager: ConnectionManager,
    private val prefs: ConnectionPrefs,
    private val settingsPrefs: SettingsPrefs,
    private val snoozeHandler: SnoozeHandler,
    private val usageProvider: UsageProvider,
    private val dipPrefs: DipPrefs,
    private val renewDipUseCase: RenewDipUseCase,
    private val customizationPrefs: CustomizationPrefs,
    private val vpnRegionPrefs: VpnRegionPrefs,
    private val ratingTool: RatingTool,
    private val shortcutPrefs: ShortcutPrefs,
    private val buildConfigProvider: BuildConfigProvider,
    val connectionInfoProvider: ConnectionInfoProvider,
    networkConnectionListener: NetworkConnectionListener,
) : ViewModel() {
    private var loadVpnServersJob: Job? = null
    private var loadShadowsocksServersJob: Job? = null
    private var updateStateJob: Job? = null
    private val isAutoMode get() = prefs.getSelectedVpnServer() == null
    private val defaultState = ConnectionScreenState(
        server = prefs.getSelectedVpnServer() ?: regionListProvider.getOptimalServer(),
        quickConnectServers = getQuickConnectVpnServers(),
        isCurrentServerOptimal = isAutoMode,
        showOptimalLocationInfo = isAutoMode && regionListProvider.isDefaultList.value,
        ratingDialogType = ratingTool.showRating.value,
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
            regionListProvider.isDefaultList.collectLatest { isDefault ->
                if (isAutoMode) {
                    _state.update {
                        it.copy(
                            server = regionListProvider.getOptimalServer(),
                            isCurrentServerOptimal = true,
                            showOptimalLocationInfo = isDefault,
                        )
                    }
                }
            }
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
                    connectionManager.connect(it, false, ::callback)
                } ?: run {
                    connectionManager.connect(
                        regionListProvider.getOptimalServer(),
                        false,
                        ::callback,
                    )
                }
            }
            if (shortcutPrefs.isShortcutDisconnectVpn()) {
                shortcutPrefs.setShortcutDisconnectVpn(false)
                connectionManager.disconnect()
            }
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
        if (dipPrefs.isDipSignupEnabled(buildConfigProvider.isGoogleFlavor()) && dipPrefs.showDedicatedIpHomeBanner()) {
            showDedicatedIpHomeBanner.value = true
        }
    }

    fun hideDedicatedIpSignupBanner() {
        dipPrefs.hideDedicatedIpHomeBanner()
        showDedicatedIpHomeBanner.value = false
    }

    fun snooze(interval: Int) = snoozeHandler.setSnooze(interval)

    private fun renewDedicatedIps() = viewModelScope.launch {
        if (dipPrefs.getDedicatedIps().isNotEmpty()) {
            for (dip in dipPrefs.getDedicatedIps()) {
                renewDipUseCase.renew(dip.dipToken)
            }
        }
    }

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
        if (getFavoriteServers().size > QUICK_CONNECT_MAX_SERVERS) {
            for (index in 0 until QUICK_CONNECT_MAX_SERVERS) {
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

    fun getSelectedShadowsocksServer() = shadowsocksListProvider.getSelected()

    fun showShadowsocksRegionSelection() {
        router.updateDestination(ShadowsocksRegionSelection)
    }

    fun onSnoozeResumed() {
        connect()
    }

    fun onConnectionButtonClicked() = viewModelScope.launch {
        if (connectionInfoProvider.isInConnectState()) {
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
        _state.update { it.copy(server = server) }
        vpnRegionPrefs.selectVpnServer(server)
        connectionManager.connectJob = viewModelScope.launch {
            connectionManager.disconnect().getOrThrow()
            connectionManager.connect(server, true, ::callback)
        }
    }

    fun isPortForwardingEnabled() = settingsPrefs.isPortForwardingEnabled()

    fun isVpnServerFavorite(serverName: String, isDip: Boolean): Boolean {
        return vpnRegionPrefs.isFavorite(serverName, isDip)
    }

    private fun connect() {
        prefs.setSelectedVpnServer(state.value.server)
        prefs.addToQuickConnect(state.value.server.key, state.value.server.isDedicatedIp)
        snoozeHandler.cancelSnooze()
        connectionManager.connectJob?.cancel()
        connectionManager.connectJob = viewModelScope.launch {
            connectionManager.connect(server = state.value.server, true, ::callback)
        }
    }

    private fun disconnect() {
        if (settingsPrefs.isAutomationEnabled()) {
            prefs.disconnectedByUser(true)
        }
        connectionManager.connectJob?.cancel()
        connectionManager.connectJob = null
        viewModelScope.launch {
            connectionManager.disconnect()
        }
    }

    private fun isOptimalLocation(serverKey: String): Boolean {
        return regionListProvider.getOptimalServer().key == serverKey
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

    fun refreshState() = _state.update {
        val selectedServer = prefs.getSelectedVpnServer()
        it.copy(
            server = selectedServer ?: regionListProvider.getOptimalServer(),
            quickConnectServers = getQuickConnectVpnServers(),
            isCurrentServerOptimal = selectedServer == null,
            showOptimalLocationInfo = selectedServer == null && regionListProvider.isDefaultList.value,
        )
    }

    private fun callback() {
        viewModelScope.launch {
            connectionManager.disconnect().getOrThrow()
        }
    }
}