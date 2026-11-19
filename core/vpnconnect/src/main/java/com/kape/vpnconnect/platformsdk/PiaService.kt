package com.kape.vpnconnect.platformsdk

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import com.kape.contracts.ConnectionManager
import com.kape.contracts.di.Qualifiers
import com.kape.data.NOTIFICATION_ID
import com.kape.localprefs.prefs.ConnectionPrefs
import com.kape.localprefs.prefs.SettingsPrefs
import com.kape.network.pinning.PinnedHttpClientFactory
import com.kape.platformsdk.vpn.openvpn.OpenVpnConnectionController
import com.kape.platformsdk.vpn.service.KapeSessionController
import com.kape.platformsdk.vpn.service.KapeSystemTunnel
import com.kape.platformsdk.vpn.service.analytics.DisconnectReason
import com.kape.platformsdk.vpn.service.models.IpAddress
import com.kape.platformsdk.vpn.service.models.KapeKillSwitchMode
import com.kape.platformsdk.vpn.service.models.KapeSplitTunnelAppMode
import com.kape.platformsdk.vpn.service.models.KapeVPNConnectionStatus
import com.kape.platformsdk.vpn.service.models.KapeVpnTunnelError
import com.kape.platformsdk.vpn.wireguard.KapeWireGuardConnectionController
import com.kape.platformsdk.vpn.wireguard.WireGuardAuthenticator
import com.kape.portforwarding.data.PortForwardingManager
import com.kape.settings.data.VpnProtocols
import com.kape.vpnconnect.data.ConnectionInfoProviderImpl
import com.kape.vpnconnect.data.DebugLogCleanerImpl
import com.kape.vpnconnect.domain.ConnectionDataSource
import com.kape.vpnconnect.domain.GetActiveInterfaceDnsUseCase
import com.kape.vpnconnect.provider.UsageProviderImpl
import com.kape.vpnconnect.utils.CountryDetector
import com.kape.vpnconnect.utils.NotificationHandler
import com.kape.vpnconnect.utils.VpnNotificationManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named

class PiaService :
    VpnService(),
    KoinComponent {
    private val certificate: String by inject(named(Qualifiers.CERTIFICATE))
    private val pinnedClients: PinnedHttpClientFactory by inject()
    private val connectionSource: ConnectionDataSource by inject()
    private val settingsPrefs: SettingsPrefs by inject()
    private val connectionPrefs: ConnectionPrefs by inject()
    private val getActiveInterfaceDnsUseCase: GetActiveInterfaceDnsUseCase by inject()
    private val vpnNotificationManager: VpnNotificationManager by inject()
    private val configureIntent: PendingIntent by inject(named(Qualifiers.VPN_NOTIFICATION_INTENT))
    private val usageProvider: UsageProviderImpl by inject()
    private val portForwardingManager: PortForwardingManager by inject()
    private val connectionManager: ConnectionManager by inject()
    private val connectionInfoProvider: ConnectionInfoProviderImpl by inject()
    private val notificationHandler: NotificationHandler by inject()
    private val countryDetector: CountryDetector by inject()
    private val clearDebugLogsUseCase: DebugLogCleanerImpl by inject()
    private var sessionController: KapeSessionController? = null
    private var statusCollectionJob: Job? = null

    // Serializes startVpn()/stopSessionController(). Overlapping calls orphaned controllers, each
    // establish() tearing down the tun the others were using.
    private val sessionMutex = Mutex()

    private val wakeLock: PowerManager.WakeLock by lazy {
        (getSystemService(Context.POWER_SERVICE) as PowerManager)
            .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "$packageName:vpnConnection")
            // Renewed on every Connecting/Reconnecting transition, but a single release() on Disconnected
            // must fully release it, so acquire()/release() need to re-enter, not stack.
            .apply { setReferenceCounted(false) }
    }

    // Both authenticators' addKey requests run before the tunnel exists and must be protect()-ed,
    // or the kill switch drops them and every WireGuard/AmneziaWG connection attempt times out.
    private val wgAuthenticator: WireGuardAuthenticator by lazy {
        PiaWgAuthenticator(
            pinnedClients,
            connectionSource,
            connectionPrefs,
            protect = ::protect,
        )
    }
    private val awgAuthenticator: WireGuardAuthenticator by lazy {
        PiaAwgAuthenticator(
            pinnedClients,
            connectionSource,
            connectionPrefs,
            protect = ::protect,
        )
    }

    private val _connectionStatus = MutableStateFlow(KapeVPNConnectionStatus.Disconnected)
    val connectionStatus: StateFlow<KapeVPNConnectionStatus> = _connectionStatus.asStateFlow()

    private val _lastTunnelError = MutableStateFlow<KapeVpnTunnelError?>(null)
    val lastTunnelError: StateFlow<KapeVpnTunnelError?> = _lastTunnelError.asStateFlow()

    private val job = SupervisorJob()
    val scope = CoroutineScope(Dispatchers.IO + job)

    private val trafficStatsDispatcher = Dispatchers.IO.limitedParallelism(2)

    inner class LocalBinder : Binder() {
        fun getService(): PiaService = this@PiaService
    }

    private val binder = LocalBinder()

    init {
        scope.launch {
            connectionStatus.collectLatest { status ->
                if (status == KapeVPNConnectionStatus.Connected) {
                    val ip = sessionController?.getGatewayForCurrentConnection()
                    if (connectionPrefs.getGatewayNow().isEmpty()) {
                        connectionPrefs.setGateway(ip?.asString() ?: "")
                    }
                    connectionPrefs.gateway.first { it.isNotEmpty() }
                    startPortForwarding()
                }
            }
        }
    }

    override fun onBind(intent: Intent): IBinder = binder

    @SuppressLint("ForegroundServiceType")
    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int,
    ): Int {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "VPN", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
        val notification = vpnNotificationManager.updateContentIntent(configureIntent)
        startForeground(NOTIFICATION_ID, notification)

        // The system starts this directly (Always-on VPN, boot, a START_STICKY restart): EXTRA_MANUAL_START
        // is only set by our own connect flow, so its absence means the system expects a connection.
        if (intent?.getBooleanExtra(EXTRA_MANUAL_START, false) != true) {
            scope.launch { connectionManager.connectToLastKnownOrOptimalServer() }
        }

        return START_STICKY
    }

    suspend fun startVpn(vpnExcluded: List<String>) = sessionMutex.withLock { startVpnLocked(vpnExcluded) }

    private suspend fun startVpnLocked(vpnExcluded: List<String>) {
        clearDebugLogsUseCase.clearIfScheduled()
        sessionController?.stop()
        sessionController = null
        statusCollectionJob?.cancel()
        statusCollectionJob = null
        _lastTunnelError.update { null }

        wakeLock.acquire(WAKE_LOCK_TIMEOUT_MS)

        notificationHandler.updateConnectionInfo(
            getString(
                com.kape.ui.R.string.vpn_notification_title_format,
                connectionInfoProvider.name,
            ),
            configureIntent,
        )

        val killSwitchMode =
            if (settingsPrefs.isAllowLocalTrafficEnabledNow()) {
                KapeKillSwitchMode.Standard
            } else {
                KapeKillSwitchMode.Advanced
            }

        val selectedProtocol = settingsPrefs.getSelectedProtocolNow()
        val vpnServiceLogger =
            ServiceLogger(
                this,
                when (selectedProtocol) {
                    VpnProtocols.WireGuard -> ServiceLogger.VpnServiceLoggerTag.WireGuard
                    VpnProtocols.OpenVPN -> ServiceLogger.VpnServiceLoggerTag.OpenVpn
                    VpnProtocols.Automatic -> ServiceLogger.VpnServiceLoggerTag.Automatic
                },
            )

        val systemTunnel =
            KapeSystemTunnel(
                this,
                vpnServiceLogger,
                killSwitchMode = killSwitchMode,
                splitTunnelAppMode =
                    if (vpnExcluded.isEmpty()) {
                        KapeSplitTunnelAppMode.Off
                    } else {
                        KapeSplitTunnelAppMode.Disallow(
                            vpnExcluded,
                        )
                    },
            )
        val authenticator =
            CompositeWireGuardAuthenticator(
                wgAuthenticator = wgAuthenticator,
                awgAuthenticator = awgAuthenticator,
            )
        val wireGuardController =
            KapeWireGuardConnectionController(
                systemTunnel = systemTunnel,
                authenticator = authenticator,
                logger = vpnServiceLogger,
            )
        val openVpnController =
            OpenVpnConnectionController(
                context = this,
                systemTunnel = systemTunnel,
                coroutineScope = scope,
                logger = vpnServiceLogger,
            )

        val configurationGenerator =
            ConfigurationGenerator(
                certificate,
                connectionSource,
                settingsPrefs,
                connectionPrefs,
                getActiveInterfaceDnsUseCase,
                this,
                countryDetector,
            )
        val controller =
            KapeSessionController(
                configurationGenerator = configurationGenerator,
                connectionControllers = listOf(openVpnController, wireGuardController),
                systemTunnel = systemTunnel,
                logger = vpnServiceLogger,
            )
        // attemptReporter defaults to null and isn't set by KapeSessionController itself. Without it
        // every ConnectionEndEvent and SessionEndEvent went out with wasConnected=false.
        openVpnController.attemptReporter = controller
        wireGuardController.attemptReporter = controller

        sessionController = controller

        scope.launch(trafficStatsDispatcher) {
            sessionController?.state?.trafficStats?.collectLatest {
                usageProvider.byteCount(it.bytesSent, it.bytesReceived)
            }
        }

        statusCollectionJob =
            scope.launch {
                launch {
                    controller.state.connectionStatus.collect { status ->
                        _connectionStatus.update { status }
                        when (status) {
                            KapeVPNConnectionStatus.Connecting,
                            KapeVPNConnectionStatus.Reconnecting,
                            -> wakeLock.acquire(WAKE_LOCK_TIMEOUT_MS)
                            KapeVPNConnectionStatus.Disconnected ->
                                if (wakeLock.isHeld) wakeLock.release()

                            else -> Unit
                        }
                    }
                }
                launch {
                    controller.state.lastTunnelError.collect { error -> _lastTunnelError.update { error } }
                }
            }

        scope.launch { controller.start() }
    }

    suspend fun stopSessionController(reason: DisconnectReason = DisconnectReason.UserInitiated) =
        sessionMutex.withLock {
            stopSessionControllerLocked(reason)
        }

    private suspend fun stopSessionControllerLocked(reason: DisconnectReason) {
        statusCollectionJob?.cancel()
        statusCollectionJob = null
        sessionController?.stop(reason)
        sessionController = null
        usageProvider.reset()
        _connectionStatus.update { KapeVPNConnectionStatus.Disconnected }
        _lastTunnelError.update { null }
        if (wakeLock.isHeld) wakeLock.release()
    }

    override fun onDestroy() {
        super.onDestroy()
        scope
            .launch {
                stopSessionController()
                clearDebugLogsUseCase.clearIfScheduled()
            }.invokeOnCompletion { job.cancel() }
    }

    override fun onRevoke() {
        scope
            .launch { stopSessionController(DisconnectReason.Revoked) }
            .invokeOnCompletion { stopSelf() }
        stopSelf()
    }

    private suspend fun startPortForwarding() {
        if (!settingsPrefs.isPortForwardingEnabledNow()) return
        portForwardingManager.bindPort(connectionSource.getVpnToken())
        connectionSource.startPortForwarding()
    }

    private fun IpAddress.asString(): String =
        when (this) {
            is IpAddress.V4 -> value
            is IpAddress.V6 -> value
        }

    companion object {
        private const val CHANNEL_ID = "kape_vpn"
        const val EXTRA_MANUAL_START = "manual_start"

        private const val WAKE_LOCK_TIMEOUT_MS = 5 * 60 * 1000L
    }
}