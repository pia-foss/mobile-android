package com.kape.connection.ui.vm

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kape.connection.domain.usecase.ConnectOnLaunchUseCase
import com.kape.connection.domain.usecase.ConnectUseCase
import com.kape.connection.domain.usecase.DisconnectUseCase
import com.kape.connection.domain.usecase.ObserveDisplayedServerUseCase
import com.kape.connection.domain.usecase.ObserveScreenElementsUseCase
import com.kape.connection.domain.usecase.ObserveShortcutRequestsUseCase
import com.kape.connection.domain.usecase.QuickConnectUseCase
import com.kape.connection.domain.usecase.ShortcutRequest
import com.kape.connection.domain.usecase.SwitchToAutomaticProtocolUseCase
import com.kape.connection.ui.tiles.TileActions
import com.kape.contracts.AppUpdateStatus
import com.kape.contracts.ConnectionInfoProvider
import com.kape.contracts.DedicatedIpHome
import com.kape.contracts.NetworkStatus
import com.kape.contracts.PaymentIssueChecker
import com.kape.contracts.PermissionStatus
import com.kape.contracts.Router
import com.kape.data.AutomationSettings
import com.kape.data.ConnectionStatus
import com.kape.data.Customization
import com.kape.data.DedicatedIpSignupPlans
import com.kape.data.HelpSettings
import com.kape.data.KillSwitchSettings
import com.kape.data.ProtocolSettings
import com.kape.data.Settings
import com.kape.data.ShadowsocksRegionSelection
import com.kape.data.TvSideMenu
import com.kape.data.VpnPermission
import com.kape.data.VpnRegionSelection
import com.kape.data.vpnserver.VpnServer
import com.kape.localprefs.data.customization.ScreenElement
import com.kape.rating.data.RatingDialogType
import com.kape.rating.utils.RatingTool
import com.kape.snooze.SnoozeHandler
import com.kape.vpnconnect.domain.ConnectionProblemDetector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class ConnectionViewModel(
    private val router: Router,
    private val permissions: PermissionStatus,
    private val connectionInfo: ConnectionInfoProvider,
    private val connect: ConnectUseCase,
    private val disconnect: DisconnectUseCase,
    private val quickConnect: QuickConnectUseCase,
    private val switchToAutomaticProtocol: SwitchToAutomaticProtocolUseCase,
    private val snooze: SnoozeHandler,
    private val ratingTool: RatingTool,
    private val problemDetector: ConnectionProblemDetector,
    private val dedicatedIpHome: DedicatedIpHome,
    private val appUpdateStatus: AppUpdateStatus,
    private val paymentIssueChecker: PaymentIssueChecker,
    observeDisplayedServer: ObserveDisplayedServerUseCase,
    observeScreenElements: ObserveScreenElementsUseCase,
    connectOnLaunch: ConnectOnLaunchUseCase,
    observeShortcutRequests: ObserveShortcutRequestsUseCase,
    networkStatus: NetworkStatus,
) : ViewModel(),
    TileActions {
    val status: StateFlow<ConnectionStatus> = connectionInfo.status
    val isOnline: StateFlow<Boolean> = networkStatus.isOnline
    val hasUpdateAvailable: StateFlow<Boolean> = appUpdateStatus.hasUpdateAvailable
    val showAutoProtocolNudge: StateFlow<Boolean> = problemDetector.showNudge

    val elements: StateFlow<List<ScreenElement>> =
        observeScreenElements().map { it.connection }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    val showDedicatedIpBanner: StateFlow<Boolean> =
        dedicatedIpHome.observeSignupBannerVisible().stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), false)

    private val _showProtocolNotAvailable = MutableStateFlow(false)
    val showProtocolNotAvailable: StateFlow<Boolean> = _showProtocolNotAvailable.asStateFlow()

    private val _ratingPrompt = MutableStateFlow<RatingDialogType?>(null)
    val ratingPrompt: StateFlow<RatingDialogType?> = _ratingPrompt.asStateFlow()

    private val displayedServer = observeDisplayedServer().stateIn(viewModelScope, SharingStarted.Eagerly, null)

    init {
        if (!permissions.isVpnProfileInstalled()) {
            router.updateDestination(VpnPermission)
        }
        connectionInfo.requestClientIp()
        viewModelScope.launch { dedicatedIpHome.renewDedicatedIps() }
        viewModelScope.launch { ratingTool.showRating.collect { _ratingPrompt.value = it } }
        viewModelScope.launch { connectOnLaunch(::onProtocolNotAvailable) }
        viewModelScope.launch { observeShortcutRequests().collect(::onShortcutRequest) }
    }

    fun onConnectionButtonClicked() {
        when {
            !permissions.isVpnProfileInstalled() -> router.updateDestination(VpnPermission)
            connectionInfo.isInConnectState() -> disconnect()
            else -> connectToDisplayedServer()
        }
    }

    private fun connectToDisplayedServer() {
        connect(displayedServer.value?.server, onProtocolNotAvailable = ::onProtocolNotAvailable)
    }

    private fun onShortcutRequest(request: ShortcutRequest) {
        when (request) {
            ShortcutRequest.Connect -> if (!connectionInfo.isConnected()) connectToDisplayedServer()
            ShortcutRequest.Disconnect -> if (connectionInfo.shouldAllowDisconnect()) disconnect()
            ShortcutRequest.ChangeServer -> onRegionClick()
            ShortcutRequest.OpenSettings -> router.updateDestination(Settings)
        }
    }

    // The problem detector already counted it: ConnectionManager reports it before calling back.
    private fun onProtocolNotAvailable() {
        _showProtocolNotAvailable.value = true
    }

    fun dismissProtocolNotAvailable() {
        _showProtocolNotAvailable.value = false
    }

    fun acceptAutomaticProtocol() {
        switchToAutomaticProtocol(displayedServer.value?.server)
    }

    fun dismissAutoProtocolNudge() {
        viewModelScope.launch { problemDetector.onNudgeDismissed() }
    }

    fun checkForPaymentIssues(activity: Activity) = paymentIssueChecker.checkForPaymentIssues(activity)

    suspend fun getDownloadUrl(): String? = appUpdateStatus.getDownloadUrl()

    fun hideDedicatedIpBanner() {
        viewModelScope.launch { dedicatedIpHome.hideSignupBanner() }
    }

    fun onRatingThumbsUp() {
        _ratingPrompt.value = RatingDialogType.Review
    }

    fun onRatingThumbsDown() {
        _ratingPrompt.value = RatingDialogType.Feedback
    }

    fun onRatingFinished() {
        _ratingPrompt.value = null
        viewModelScope.launch { ratingTool.setRatingInactive() }
    }

    fun onFeedbackFinished() {
        _ratingPrompt.value = null
        viewModelScope.launch { ratingTool.updateRatingDate() }
    }

    override fun onRegionClick() = withVpnPermission { router.updateDestination(VpnRegionSelection) }

    override fun onShadowsocksRegionClick() = router.updateDestination(ShadowsocksRegionSelection)

    override fun onQuickConnect(server: VpnServer) = withVpnPermission { quickConnect(server) }

    override fun onSnooze(intervalMillis: Int) = snooze.setSnooze(intervalMillis)

    override fun onResumeSnooze() = connectToDisplayedServer()

    override fun onKillSwitchClick() = router.updateDestination(KillSwitchSettings)

    override fun onAutomationClick() = router.updateDestination(AutomationSettings)

    override fun onProtocolsClick() = router.updateDestination(ProtocolSettings)

    fun navigateToCustomization() = router.updateDestination(Customization)

    fun navigateToDedicatedIpPlans() = router.updateDestination(DedicatedIpSignupPlans)

    fun navigateToSideMenu() = router.updateDestination(TvSideMenu)

    fun navigateToHelp() = router.updateDestination(HelpSettings)

    private fun withVpnPermission(action: () -> Unit) {
        if (permissions.isVpnProfileInstalled()) action() else router.updateDestination(VpnPermission)
    }
}

private const val STOP_TIMEOUT_MS = 5_000L