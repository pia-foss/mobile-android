package com.kape.vpnconnect.platformsdk

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.net.VpnService
import android.os.Binder
import android.os.Build
import android.os.IBinder
import com.kape.contracts.ConfigInfo
import com.kape.contracts.ConnectionInfoProvider
import com.kape.contracts.ConnectionManager
import com.kape.contracts.KpiDataSource
import com.kape.contracts.UsageProvider
import com.kape.data.NOTIFICATION_ID
import com.kape.localprefs.prefs.ConnectionPrefs
import com.kape.localprefs.prefs.ConsentPrefs
import com.kape.localprefs.prefs.SettingsPrefs
import com.kape.platformsdk.vpn.openvpn.OpenVpnConnectionController
import com.kape.platformsdk.vpn.service.KapeSessionController
import com.kape.platformsdk.vpn.service.KapeSystemTunnel
import com.kape.platformsdk.vpn.service.analytics.DisconnectReason
import com.kape.platformsdk.vpn.service.models.IpAddress
import com.kape.platformsdk.vpn.service.models.KapeKillSwitchMode
import com.kape.platformsdk.vpn.service.models.KapeSplitTunnelAppMode
import com.kape.platformsdk.vpn.service.models.KapeVPNConnectionStatus
import com.kape.platformsdk.vpn.wireguard.KapeWireGuardConnectionController
import com.kape.portforwarding.domain.PortForwardingUseCase
import com.kape.settings.data.DnsOptions
import com.kape.settings.data.VpnProtocols
import com.kape.utils.VpnNotificationManager
import com.kape.vpnconnect.domain.ConnectionDataSource
import com.kape.vpnconnect.domain.GetActiveInterfaceDnsUseCase
import com.kape.vpnconnect.utils.CountryDetector
import com.kape.vpnconnect.utils.NotificationHandler
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
import org.koin.core.annotation.Singleton
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@Singleton
class PiaService :
    VpnService(),
    KoinComponent {
    private val configInfo: ConfigInfo by inject()
    private val connectionSource: ConnectionDataSource by inject()
    private val settingsPrefs: SettingsPrefs by inject()
    private val connectionPrefs: ConnectionPrefs by inject()
    private val consentPrefs: ConsentPrefs by inject()
    private val getActiveInterfaceDnsUseCase: GetActiveInterfaceDnsUseCase by inject()
    private val vpnNotificationManager: VpnNotificationManager by inject()
    private val configureIntent: PendingIntent by inject()
    private val usageProvider: UsageProvider by inject()
    private val portForwardingUseCase: PortForwardingUseCase by inject()
    private val connectionManager: ConnectionManager by inject()
    private val connectionInfoProvider: ConnectionInfoProvider by inject()
    private val notificationHandler: NotificationHandler by inject()
    private val countryDetector: CountryDetector by inject()
    private val kpiDataSource: KpiDataSource by inject()
    private var sessionController: KapeSessionController? = null
    private var statusCollectionJob: Job? = null

    private val _connectionStatus = MutableStateFlow(KapeVPNConnectionStatus.Disconnected)
    val connectionStatus: StateFlow<KapeVPNConnectionStatus> = _connectionStatus.asStateFlow()

    private val job = SupervisorJob()
    val scope = CoroutineScope(Dispatchers.IO + job)

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

    // VpnService has system-level recognition via BIND_VPN_SERVICE and is exempt from the
    // standard foregroundServiceType requirement. No declared type fits a VPN tunnel semantically.
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

        // This is the component flagged android.net.VpnService.SUPPORTS_ALWAYS_ON, so the system
        // starts it directly (Always-on VPN, boot, a START_STICKY restart) without going through
        // ConnectionManagerImpl.connect() first — EXTRA_MANUAL_START is only ever set by our own
        // connect flow, so its absence means the system started us and expects a connection.
        if (intent?.getBooleanExtra(EXTRA_MANUAL_START, false) != true) {
            scope.launch { connectionManager.connectToLastKnownOrOptimalServer() }
        }

        return START_STICKY
    }

    suspend fun startVpn(
        selectedDnsOptions: DnsOptions,
        vpnExcluded: List<String>,
    ) {
        sessionController?.stop()
        sessionController = null
        statusCollectionJob?.cancel()
        statusCollectionJob = null

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
                // Advanced routes 0.0.0.0/0 with no local-range carve-out, so LAN traffic is
                // blocked rather than allowed to bypass the tunnel.
                KapeKillSwitchMode.Advanced
            }

        val vpnServiceLogger =
            ServiceLogger(
                when (settingsPrefs.getSelectedProtocolNow()) {
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
            PiaWgAuthenticator(
                selectedDnsOptions,
                configInfo.certificate,
                connectionSource,
                connectionPrefs,
                protect = systemTunnel::protect,
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
                configInfo.certificate,
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
            )

        sessionController = controller

        scope.launch {
            sessionController?.state?.trafficStats?.collectLatest {
                usageProvider.byteCount(it.bytesSent, it.bytesReceived)
            }
        }

        statusCollectionJob =
            scope.launch {
                controller.state.connectionStatus.collect { status ->
                    _connectionStatus.update { status }
                }
            }

        scope.launch { controller.start() }
    }

    suspend fun stopSessionController(reason: DisconnectReason = DisconnectReason.UserInitiated) {
        statusCollectionJob?.cancel()
        statusCollectionJob = null
        sessionController?.stop(reason)
        sessionController = null
        usageProvider.reset()
        _connectionStatus.update { KapeVPNConnectionStatus.Disconnected }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.launch { stopSessionController() }.invokeOnCompletion { job.cancel() }
    }

    override fun onRevoke() {
        scope
            .launch { stopSessionController(DisconnectReason.Revoked) }
            .invokeOnCompletion { stopSelf() }
        stopSelf()
    }

    private suspend fun startPortForwarding() {
        if (!settingsPrefs.isPortForwardingEnabledNow()) return
        portForwardingUseCase.bindPort(connectionSource.getVpnToken())
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
    }
}