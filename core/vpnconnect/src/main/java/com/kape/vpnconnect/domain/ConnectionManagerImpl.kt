package com.kape.vpnconnect.domain

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.core.content.ContextCompat
import com.kape.contracts.ConnectionInfoProvider
import com.kape.contracts.ConnectionManager
import com.kape.contracts.ConnectionStatusProvider
import com.kape.data.DI
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
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import java.util.concurrent.atomic.AtomicBoolean

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

    private val vpnScope: CoroutineScope by inject(named(DI.IO_SCOPE))

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

    /**
     * Conflated channel ensures:
     * - rapid reconnect calls overwrite previous ones
     * - only the latest request is processed
     */
    private val reconnectChannel =
        Channel<Pair<VpnServer, () -> Unit>>(capacity = Channel.CONFLATED)

    private val connectionInProgress = AtomicBoolean(false)

    override var connectJob: Job? = null

    init {
        startReconnectProcessor()
    }

    /**
     * Single consumer loop that guarantees:
     * - sequential VPN transitions
     * - no UI blocking
     * - no reconnect storms
     */
    private fun startReconnectProcessor() {
        vpnScope.launch {
            for ((server, stopCallback) in reconnectChannel) {
                handleReconnect(server, stopCallback)
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun handleReconnect(
        server: VpnServer,
        stopCallback: () -> Unit,
    ) {
        connectionPrefs.addToQuickConnect(server.key, server.isDedicatedIp)

        disconnect()

        // A newer reconnect request arrived during disconnect; let the loop handle it.
        if (!reconnectChannel.isEmpty) return

        try {
            connect(
                server,
                isManual = true,
                stopCallback,
                {
                    // TODO: this function will be used as the upcoming fallback implementation
                },
            )
        } catch (_: Exception) {
            // Swallow to keep processor alive and allow newer state to apply
        }
    }

    override suspend fun connect(
        server: VpnServer,
        isManual: Boolean,
        stopCallback: () -> Unit,
        showDialog: () -> Unit,
    ) {
        if (server.endpoints[mapProtocolToServerGroup()].isNullOrEmpty()) {
            showDialog()
        } else {
            connectionInProgress.set(true)

            connectionInfoProvider.updateInfo(server.name, server.iso, isManual)
            connectionPrefs.setSelectedVpnServer(server)
            connectionPrefs.addToQuickConnect(server.key, server.isDedicatedIp)

            val dns = settingsPrefs.getSelectedDnsOptionNow()
            val shadowsocksOk = startShadowsocks(stopCallback)
            if (!shadowsocksOk) {
                connectionInProgress.set(false)
                return
            }

            scope.launch {
                val service = startServiceIfNeeded()
                println("--- service started if needed")
                service.startVpn(dns)
            }

//            val clientConfiguration =
//                connectionConfigurationUseCase.generateConnectionConfiguration(server)
//            connectionSource
//                .startConnection(
//                    clientConfiguration,
//                    connectionStatusProvider,
//                ).fold(
//                    onSuccess = { startPortForwarding() },
//                    onFailure = { disconnect().getOrNull() },
//                )
        }
    }

    override suspend fun disconnect() {
        scope.launch {
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
        }
    }

    /**
     * Non-blocking:
     * - only enqueues latest server request
     * - never suspends caller
     */
    override suspend fun reconnect(
        server: VpnServer,
        stopCallback: () -> Unit,
    ) {
        connectionPrefs.addToQuickConnect(server.key, server.isDedicatedIp)

        reconnectChannel.trySend(server to stopCallback)
    }

    override fun isConnectionInProgress(): Boolean = connectionInProgress.get()

    // ───────────────────────────────────────────────────────────────
    // Private helpers
    // ───────────────────────────────────────────────────────────────

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
                println("--- onServiceConnected")
                val service = (binder as PiaService.LocalBinder).getService()
                boundService = service
                serviceDeferred?.complete(service)
                serviceDeferred = null
                statusCollectionJob =
                    scope.launch {
                        service.connectionStatus.collect { status ->
                            _connectionStatus.update { status }
                            if (status == KapeVPNConnectionStatus.Connected) {
                                println("--- gateway: ${connectionPrefs.getGatewayNow()}")
                            }
                            connectionStatusProvider.handleConnectionStatusChange(status)
                        }
                    }
            }

            override fun onServiceDisconnected(name: ComponentName) {
                println("--- onServiceDisconnected")
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
                Intent(context, PiaService::class.java),
            )
            context.bindService(
                Intent(context, PiaService::class.java),
                serviceConnection,
                Context.BIND_AUTO_CREATE,
            )
        }

        return deferred.await()
    }
}