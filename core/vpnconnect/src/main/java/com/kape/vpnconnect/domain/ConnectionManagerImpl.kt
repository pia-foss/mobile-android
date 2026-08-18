package com.kape.vpnconnect.domain

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.core.content.ContextCompat
import com.kape.contracts.AuthenticationDataSource
import com.kape.contracts.ConnectionInfoProvider
import com.kape.contracts.ConnectionManager
import com.kape.contracts.ConnectionStatusProvider
import com.kape.data.vpnserver.VpnServer
import com.kape.localprefs.prefs.ConnectionPrefs
import com.kape.localprefs.prefs.SettingsPrefs
import com.kape.localprefs.prefs.ShadowsocksRegionPrefs
import com.kape.obfuscator.data.ObfuscatorProcessInformation
import com.kape.obfuscator.data.ObfuscatorProcessListener
import com.kape.obfuscator.domain.StartObfuscatorProcess
import com.kape.obfuscator.domain.StopObfuscatorProcess
import com.kape.platformsdk.vpn.service.models.KapeVPNConnectionStatus
import com.kape.portforwarding.domain.PortForwardingUseCase
import com.kape.settings.data.Transport
import com.kape.settings.data.VpnProtocols
import com.kape.vpnconnect.platformsdk.PiaService
import com.kape.vpnregions.utils.RegionListProvider
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.time.Duration.Companion.milliseconds

class ConnectionManagerImpl :
    ConnectionManager,
    KoinComponent {
    private val connectionSource: ConnectionDataSource by inject()
    private val connectionInfoProvider: ConnectionInfoProvider by inject()
    private val connectionPrefs: ConnectionPrefs by inject()
    private val settingsPrefs: SettingsPrefs by inject()
    private val shadowsocksRegionPrefs: ShadowsocksRegionPrefs by inject()
    private val startObfuscatorProcess: StartObfuscatorProcess by inject()
    private val stopObfuscatorProcess: StopObfuscatorProcess by inject()
    private val portForwardingUseCase: PortForwardingUseCase by inject()
    private val connectionStatusProvider: ConnectionStatusProvider by inject()
    private val regionListProvider: RegionListProvider by inject()
    private val authenticationDataSource: AuthenticationDataSource by inject()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val context: Context by inject()

    private val _connectionStatus = MutableStateFlow(KapeVPNConnectionStatus.Disconnected)
    val connectionStatus: StateFlow<KapeVPNConnectionStatus> = _connectionStatus.asStateFlow()

    private var statusCollectionJob: Job? = null

    private var bindAttempted = false
    private var boundService: PiaService? = null

    // Completed by onServiceConnected; awaited by startServiceIfNeeded().
    // Non-null only while a bind is in progress (service not yet connected).
    private var serviceDeferred: CompletableDeferred<PiaService>? = null

    // The in-flight connect() attempt, if any. disconnect() cancels it so a newer
    // request always supersedes one that hasn't reached Connected yet.
    private var connectionJob: Job? = null

    private val connectionInProgress = AtomicBoolean(false)

    override suspend fun connect(
        server: VpnServer,
        isManual: Boolean,
        stopCallback: () -> Unit,
        showDialog: () -> Unit,
    ) {
        if (server.endpoints[mapProtocolToServerGroup()].isNullOrEmpty()) {
            showDialog()
            return
        }

        connectionInProgress.set(true)

        connectionInfoProvider.updateInfo(server.name, server.iso, isManual)
        connectionPrefs.setSelectedVpnServer(server)
        connectionPrefs.addToQuickConnect(server.key, server.isDedicatedIp)

        val dns = settingsPrefs.getSelectedDnsOptionNow()
        if (!startShadowsocks(stopCallback)) {
            connectionInProgress.set(false)
            return
        }

        connectionJob =
            scope.launch {
                val service = startServiceIfNeeded()
                val excluded = settingsPrefs.getVpnExcludedAppsNow()
                service.startVpn(dns, excluded)
            }
        connectionJob?.join()
    }

    override suspend fun connectToLastKnownOrOptimalServer() {
        if (!isUserLoggedInWithRetry()) return

        if (settingsPrefs.isAutomationEnabledNow() && connectionPrefs.isDisconnectedByUser.value) {
            connectionPrefs.setDisconnectedByUser(false)
            return
        }

        val server =
            connectionPrefs.getSelectedVpnServerNow()
                ?: run {
                    if (regionListProvider.isDefaultList.first().not()) {
                        regionListProvider.getOptimalServer()
                    } else {
                        regionListProvider.updateServerLatencies(isConnected = false, isUserInitiated = false)
                        regionListProvider.getOptimalServer()
                    }
                }

        connect(
            server,
            isManual = false,
            stopCallback = { scope.launch { disconnect() } },
            showDialog = {
                // no-op for now, might be used for fallback
            },
        )
    }

    override suspend fun disconnect() {
        connectionJob?.cancelAndJoin()
        connectionJob = null

        scope
            .launch {
                serviceDeferred?.cancel()
                serviceDeferred = null
                boundService?.stopSessionController()
                statusCollectionJob?.cancel()
                statusCollectionJob = null
                if (bindAttempted) {
                    bindAttempted = false
                    boundService = null
                    context.unbindService(serviceConnection)
                }
                context.stopService(Intent(context, PiaService::class.java))
                _connectionStatus.update { KapeVPNConnectionStatus.Disconnected }
                connectionStatusProvider.handleConnectionStatusChange(KapeVPNConnectionStatus.Disconnected)
                stopObfuscatorProcess()
                cancelPortForwarding()
                connectionInfoProvider.resetConnectionInfo()
                connectionInProgress.set(false)
            }.join()
    }

    override suspend fun reconnect(
        server: VpnServer,
        stopCallback: () -> Unit,
    ) {
        connectionPrefs.addToQuickConnect(server.key, server.isDedicatedIp)

        disconnect()

        connect(
            server,
            isManual = true,
            stopCallback,
            showDialog = {
                // TODO: this function will be used as the upcoming fallback implementation
            },
        )
    }

    override fun isConnectionInProgress(): Boolean = connectionInProgress.get()

    // Immediately after boot, the account SDK's encrypted token store can transiently fail to
    // read (Keystore not yet warmed up), making isUserLoggedIn() falsely report false. Retry
    // briefly before treating the user as logged out.
    private suspend fun isUserLoggedInWithRetry(): Boolean {
        repeat(LOGIN_CHECK_ATTEMPTS) {
            if (authenticationDataSource.isUserLoggedIn()) return true
            delay(LOGIN_CHECK_RETRY_DELAY_MS.milliseconds)
        }
        return false
    }

    private suspend fun startShadowsocks(stopCallback: () -> Unit): Boolean {
        if (!settingsPrefs.isShadowsocksObfuscationEnabledNow()) return true

        val server =
            shadowsocksRegionPrefs.getSelectedShadowsocksServerNow() ?: return false

        return startObfuscatorProcess(
            obfuscatorProcessInformation =
                ObfuscatorProcessInformation(
                    serverIp = server.host,
                    serverPort = server.port.toString(),
                    serverKey = server.key,
                    serverEncryptMethod = server.cipher,
                ),
            obfuscatorProcessListener =
                object : ObfuscatorProcessListener {
                    override fun processStopped() = stopCallback()
                },
        ).isSuccess
    }

    private fun cancelPortForwarding(): Result<Unit> {
        connectionSource.stopPortForwarding()
        portForwardingUseCase.clearBindPort()
        return Result.success(Unit)
    }

    private fun mapProtocolToServerGroup(): VpnServer.ServerGroup =
        when (settingsPrefs.selectedProtocol.value) {
            VpnProtocols.WireGuard -> VpnServer.ServerGroup.WIREGUARD
            VpnProtocols.OpenVPN -> {
                when (settingsPrefs.openVpnSettings.value.transport) {
                    Transport.UDP -> VpnServer.ServerGroup.OPENVPN_UDP
                    Transport.TCP -> VpnServer.ServerGroup.OPENVPN_TCP
                }
            }
        }

    private val serviceConnection =
        object : ServiceConnection {
            override fun onServiceConnected(
                name: ComponentName,
                binder: IBinder,
            ) {
                val service = (binder as PiaService.LocalBinder).getService()
                boundService = service
                serviceDeferred?.complete(service)
                serviceDeferred = null
                statusCollectionJob =
                    scope.launch {
                        service.connectionStatus.collect { status ->
                            _connectionStatus.update { status }
                            connectionStatusProvider.handleConnectionStatusChange(status)
                        }
                    }
            }

            override fun onServiceDisconnected(name: ComponentName) {
                boundService = null
                statusCollectionJob?.cancel()
                statusCollectionJob = null
                _connectionStatus.update { KapeVPNConnectionStatus.Disconnected }
                connectionStatusProvider.handleConnectionStatusChange(KapeVPNConnectionStatus.Disconnected)
            }
        }

    private suspend fun startServiceIfNeeded(): PiaService {
        boundService?.let { return it }

        val deferred =
            serviceDeferred ?: CompletableDeferred<PiaService>().also { serviceDeferred = it }

        if (!bindAttempted) {
            bindAttempted = true
            ContextCompat.startForegroundService(
                context,
                Intent(context, PiaService::class.java)
                    .putExtra(PiaService.EXTRA_MANUAL_START, true),
            )
            context.bindService(
                Intent(context, PiaService::class.java),
                serviceConnection,
                Context.BIND_AUTO_CREATE,
            )
        }

        return deferred.await()
    }

    companion object {
        private const val LOGIN_CHECK_ATTEMPTS = 5
        private const val LOGIN_CHECK_RETRY_DELAY_MS = 300L
    }
}