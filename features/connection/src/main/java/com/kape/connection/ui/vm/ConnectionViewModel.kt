package com.kape.connection.ui.vm

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kape.buildconfig.data.BuildConfigProvider
import com.kape.connection.model.QuickConnectServer
import com.kape.connection.utils.ConnectionScreenState
import com.kape.contracts.ConnectionInfoProvider
import com.kape.contracts.ConnectionManager
import com.kape.contracts.Router
import com.kape.customization.data.Element
import com.kape.data.AutomationSettings
import com.kape.data.Customization
import com.kape.data.DI
import com.kape.data.DedicatedIpSignupPlans
import com.kape.data.HelpSettings
import com.kape.data.KillSwitchSettings
import com.kape.data.ProtocolSettings
import com.kape.data.QUICK_CONNECT_MAX_SERVERS
import com.kape.data.Settings
import com.kape.data.ShadowsocksRegionSelection
import com.kape.data.TvSideMenu
import com.kape.data.VpnPermission
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
import com.kape.permissions.domain.IsVpnProfileInstalledUseCase
import com.kape.rating.data.RatingDialogType
import com.kape.rating.utils.RatingTool
import com.kape.settings.data.ObfuscationOptions
import com.kape.settings.data.VpnProtocols
import com.kape.snooze.SnoozeHandler
import com.kape.utils.NetworkConnectionListener
import com.kape.vpnregions.utils.RegionListProvider
import com.kape.vpnregions.utils.ShadowsocksListProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.core.annotation.KoinViewModel
import org.koin.core.annotation.Named

@KoinViewModel
class ConnectionViewModel(
    private val router: Router,
    private val regionListProvider: RegionListProvider,
    private val shadowsocksListProvider: ShadowsocksListProvider,
    private val connectionManager: ConnectionManager,
    private val prefs: ConnectionPrefs,
    private val settingsPrefs: SettingsPrefs,
    private val snoozeHandler: SnoozeHandler,
    private val dipPrefs: DipPrefs,
    private val renewDipUseCase: RenewDipUseCase,
    private val customizationPrefs: CustomizationPrefs,
    private val vpnRegionPrefs: VpnRegionPrefs,
    private val ratingTool: RatingTool,
    private val shortcutPrefs: ShortcutPrefs,
    private val buildConfigProvider: BuildConfigProvider,
    private val isVpnProfileInstalledUseCase: IsVpnProfileInstalledUseCase,
    @Named(DI.IO_DISPATCHER) private val ioDispatcher: CoroutineDispatcher,
    val connectionInfoProvider: ConnectionInfoProvider,
    networkConnectionListener: NetworkConnectionListener,
) : ViewModel() {
    private var isAutoMode: Boolean = false
    private var selectedLocation: VpnServer? = null
    private val serverToConnectTo =
        selectedLocation?.let {
            if (it.endpoints.isNotEmpty()) {
                it
            } else {
                regionListProvider.getOptimalServer()
            }
        } ?: regionListProvider.getOptimalServer()

    private val defaultState =
        ConnectionScreenState(
            server = serverToConnectTo,
            isCurrentServerOptimal = isAutoMode,
            showOptimalLocationInfo = isAutoMode && regionListProvider.isDefaultList.value,
            ratingDialogType = ratingTool.showRating.value,
        )
    private val _state: MutableStateFlow<ConnectionScreenState> = MutableStateFlow(defaultState)
    val state: StateFlow<ConnectionScreenState> = _state
    val isConnected = networkConnectionListener.isConnected
    val isSnoozeActive = snoozeHandler.isSnoozeActive
    val timeUntilResume = snoozeHandler.timeUntilResume
    val showDedicatedIpHomeBanner = mutableStateOf(false)
    var showProtocolNotAvailableDialog = mutableStateOf(false)
        private set
    val quickConnectServers = MutableStateFlow<List<VpnServer>>(emptyList())

    init {
        if (!isVpnProfileInstalledUseCase.isVpnProfileInstalled()) {
            router.updateDestination(VpnPermission)
        }

        connectionInfoProvider.requestClientIp()

        viewModelScope.launch(ioDispatcher) {
            prefs.selectedVpnServer.collectLatest {
                it?.let {
                    isAutoMode = false
                    selectedLocation = it
                } ?: run {
                    isAutoMode = true
                }
            }
        }

        viewModelScope.launch(ioDispatcher) {
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

        viewModelScope.launch(ioDispatcher) {
            prefs.quickConnectServers.collectLatest { servers ->
                quickConnectServers.update { getQuickConnectVpnServers(servers) }
            }
        }

        ratingTool.start()
        renewDedicatedIps()

        viewModelScope.launch(ioDispatcher) {
            shortcutPrefs.isShortcutSettings.first { it }
            shortcutPrefs.setShortcutSettings(false)
            router.updateDestination(Settings)
        }

        viewModelScope.launch(ioDispatcher) {
            shortcutPrefs.isShortcutChangeServer.first { it }
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
        viewModelScope.launch(ioDispatcher) {
            combine(
                settingsPrefs.isConnectOnLaunchEnabled,
                shortcutPrefs.isShortcutConnectToVpn,
                shortcutPrefs.isShortcutDisconnectVpn,
            ) { connectOnLaunchEnabled, shortcutConnect, shortcutDisconnect ->
                if (connectOnLaunchEnabled || shortcutConnect) {
                    shortcutPrefs.setShortcutConnectToVpn(false)
                    if (!connectionInfoProvider.isConnected()) {
                        prefs.selectedVpnServer.value?.let {
                            connectionManager.connect(
                                it,
                                false,
                                ::callback,
                                ::showProtocolNotAvailable,
                            )
                        } ?: run {
                            connectionManager.connect(
                                regionListProvider.getOptimalServer(),
                                false,
                                ::callback,
                                ::showProtocolNotAvailable,
                            )
                        }
                    }
                }
                if (shortcutDisconnect) {
                    shortcutPrefs.setShortcutDisconnectVpn(false)
                    connectionManager.disconnect().getOrNull()
                }
            }.collect()
        }
    }

    fun getOrderedElements() = customizationPrefs.elements

    fun isScreenElementVisible(screenElement: ScreenElement): Boolean =
        when (screenElement.element) {
            Element.ShadowsocksRegionSelection ->
                screenElement.isVisible &&
                    settingsPrefs.isShadowsocksObfuscationEnabled.value &&
                    settingsPrefs.selectedProtocol.value == VpnProtocols.OpenVPN &&
                    settingsPrefs.selectedObfuscationOption.value == ObfuscationOptions.PIA

            Element.VpnRegionSelection,
            Element.ConnectionInfo,
            Element.QuickConnect,
            Element.QuickSettings,
            Element.Snooze,
            Element.IpInfo,
            Element.Traffic,
            -> screenElement.isVisible
        }

    fun shouldShowDedicatedIpSignupBanner() =
        viewModelScope.launch(ioDispatcher) {
            if (dipPrefs
                    .isDipSignupEnabled(buildConfigProvider.isGoogleFlavor())
                    .first() &&
                dipPrefs.dedicatedIpHomeBannerVisible.value
            ) {
                showDedicatedIpHomeBanner.value = true
            }
        }

    fun hideDedicatedIpSignupBanner() =
        viewModelScope.launch(ioDispatcher) {
            dipPrefs.hideDedicatedIpHomeBanner()
            showDedicatedIpHomeBanner.value = false
        }

    fun snooze(interval: Int) = snoozeHandler.setSnooze(interval)

    private fun renewDedicatedIps() =
        viewModelScope.launch(ioDispatcher) {
            if (dipPrefs.dedicatedIps.value.isNotEmpty()) {
                for (dip in dipPrefs.dedicatedIps.value) {
                    renewDipUseCase.renew(dip.dipToken)
                }
            }
        }

    private fun getFavoriteServers(): List<VpnServer> {
        val favoriteServers = mutableListOf<VpnServer>()
        for (item in vpnRegionPrefs.favoriteVpnServers.value) {
            regionListProvider.servers.value
                .firstOrNull { it.name == item.name && it.isDedicatedIp == item.isDip }
                ?.let {
                    favoriteServers.add(it)
                }
        }
        return favoriteServers
    }

    private fun getQuickConnectVpnServers(servers: List<QuickConnectServer>): List<VpnServer> {
        val orderedServers = mutableListOf<VpnServer>()
        if (getFavoriteServers().size > QUICK_CONNECT_MAX_SERVERS) {
            for (index in 0 until QUICK_CONNECT_MAX_SERVERS) {
                orderedServers.add(getFavoriteServers()[index])
            }
        } else {
            for (index in 0 until getFavoriteServers().size) {
                orderedServers.add(getFavoriteServers()[index])
            }
            val previousConnections = servers.reversed()
            for (server in previousConnections) {
                regionListProvider.servers.value
                    .firstOrNull { it.key == server.serverKey && it.isDedicatedIp == server.isDip }
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
        if (!isVpnProfileInstalledUseCase.isVpnProfileInstalled()) {
            router.updateDestination(VpnPermission)
        } else {
            router.updateDestination(VpnRegionSelection)
        }
    }

    fun getSelectedShadowsocksServer() = shadowsocksListProvider.getSelected()

    fun showShadowsocksRegionSelection() {
        router.updateDestination(ShadowsocksRegionSelection)
    }

    fun onSnoozeResumed() {
        connect()
    }

    fun onConnectionButtonClicked() {
        if (!isVpnProfileInstalledUseCase.isVpnProfileInstalled()) {
            router.updateDestination(VpnPermission)
        } else {
            viewModelScope.launch(ioDispatcher) {
                if (connectionInfoProvider.isInConnectState()) {
                    disconnect()
                } else {
                    connect()
                }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getConnectionSettings(): StateFlow<com.kape.settings.data.ProtocolSettings> =
        settingsPrefs.selectedProtocol
            .flatMapLatest { protocol ->
                when (protocol) {
                    VpnProtocols.WireGuard -> settingsPrefs.wireGuardSettings
                    VpnProtocols.OpenVPN -> settingsPrefs.openVpnSettings
                }
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(),
                initialValue =
                    when (settingsPrefs.selectedProtocol.value) {
                        VpnProtocols.WireGuard -> settingsPrefs.wireGuardSettings.value
                        VpnProtocols.OpenVPN -> settingsPrefs.openVpnSettings.value
                    },
            )

    fun quickConnect(server: VpnServer) {
        viewModelScope.launch(ioDispatcher) {
            if (!isVpnProfileInstalledUseCase.isVpnProfileInstalled()) {
                router.updateDestination(VpnPermission)
            } else {
                vpnRegionPrefs.selectVpnServer(server)
                connectionManager.connectJob =
                    viewModelScope.launch(ioDispatcher) {
                        connectionManager.reconnect(server, ::callback)
                    }
                _state.update {
                    it.copy(
                        server = server,
                    )
                }
            }
        }
    }

    fun isVpnServerFavorite(
        serverName: String,
        isDip: Boolean,
    ): Boolean = runBlocking { vpnRegionPrefs.isFavorite(serverName, isDip).first() }

    private fun connect() {
        viewModelScope.launch(ioDispatcher) {
            val connectTo =
                if (state.value.server.endpoints
                        .isEmpty()
                ) {
                    regionListProvider.getOptimalServer()
                } else {
                    state.value.server
                }
            prefs.setSelectedVpnServer(connectTo)
            prefs.addToQuickConnect(connectTo.key, connectTo.isDedicatedIp)
            snoozeHandler.cancelSnooze()
            connectionManager.connectJob?.cancel()
            connectionManager.connectJob =
                viewModelScope.launch(ioDispatcher) {
                    connectionManager.connect(
                        server = connectTo,
                        true,
                        ::callback,
                        ::showProtocolNotAvailable,
                    )
                }
        }
    }

    private fun disconnect() {
        viewModelScope.launch(ioDispatcher) {
            if (settingsPrefs.isAutomationEnabled.value) {
                prefs.setDisconnectedByUser(true)
            }
            connectionManager.connectJob?.cancel()
            connectionManager.connectJob = null
            viewModelScope.launch(ioDispatcher) {
                connectionManager.disconnect().getOrNull()
            }
        }
    }

    private fun isOptimalLocation(serverKey: String): Boolean = regionListProvider.getOptimalServer().key == serverKey

    fun showReviewPrompt() = _state.update { it.copy(ratingDialogType = RatingDialogType.Review) }

    fun showFeedbackPrompt() = _state.update { it.copy(ratingDialogType = RatingDialogType.Feedback) }

    fun setRatingStateInactive() =
        viewModelScope.launch(ioDispatcher) {
            ratingTool.setRatingInactive()
            _state.update { it.copy(ratingDialogType = null) }
        }

    fun updateRatingDate() =
        viewModelScope.launch(ioDispatcher) {
            ratingTool.updateRatingDate()
            _state.update { it.copy(ratingDialogType = null) }
        }

    fun showProtocolNotAvailable() {
        showProtocolNotAvailableDialog.value = true
    }

    fun resetProtocolNotAvailable() {
        showProtocolNotAvailableDialog.value = false
    }

    fun refreshState() =
        _state.update {
            val selectedServer = prefs.selectedVpnServer.value
            it.copy(
                server = selectedServer ?: regionListProvider.getOptimalServer(),
                isCurrentServerOptimal = selectedServer == null,
                showOptimalLocationInfo = selectedServer == null && regionListProvider.isDefaultList.value,
            )
        }

    private fun callback() {
        viewModelScope.launch(ioDispatcher) {
            connectionManager.disconnect().getOrNull()
        }
    }
}