package com.kape.vpnconnect.data

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.core.content.ContextCompat
import com.kape.contracts.ConnectionManager
import com.kape.contracts.IsUserLoggedInUseCase
import com.kape.contracts.VpnConnectionControl
import com.kape.contracts.di.Qualifiers
import com.kape.data.ConnectionStatus
import com.kape.data.vpnserver.VpnServer
import com.kape.localprefs.prefs.ConnectionPrefs
import com.kape.localprefs.prefs.SettingsPrefs
import com.kape.localprefs.prefs.ShadowsocksRegionPrefs
import com.kape.obfuscator.domain.ObfuscatorProcessInformation
import com.kape.obfuscator.domain.StartObfuscatorProcess
import com.kape.obfuscator.domain.StopObfuscatorProcess
import com.kape.platformsdk.vpn.service.models.KapeVPNConnectionStatus
import com.kape.portforwarding.data.PortForwardingManager
import com.kape.regions.vpn.domain.usecase.GetOptimalVpnServerUseCase
import com.kape.regions.vpn.domain.usecase.ObserveIsDefaultVpnServerListUseCase
import com.kape.regions.vpn.domain.usecase.RefreshVpnServersUseCase
import com.kape.settings.data.Transport
import com.kape.settings.data.VpnProtocols
import com.kape.vpnconnect.domain.ConnectionDataSource
import com.kape.vpnconnect.domain.ConnectionProblemDetector
import com.kape.vpnconnect.platformsdk.PiaService
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.koin.core.annotation.Named
import org.koin.core.annotation.Singleton
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean

@Singleton(binds = [ConnectionManager::class, VpnConnectionControl::class])
class ConnectionManagerImpl(
    private val context: Context,
    private val connectionSource: ConnectionDataSource,
    private val connectionInfo: ConnectionInfoProviderImpl,
    private val statusTracker: VpnStatusTracker,
    private val problemDetector: ConnectionProblemDetector,
    private val connectionPrefs: ConnectionPrefs,
    private val settingsPrefs: SettingsPrefs,
    private val shadowsocksRegionPrefs: ShadowsocksRegionPrefs,
    private val startObfuscator: StartObfuscatorProcess,
    private val stopObfuscator: StopObfuscatorProcess,
    private val portForwarding: PortForwardingManager,
    private val isUserLoggedIn: IsUserLoggedInUseCase,
    private val isDefaultVpnServerList: ObserveIsDefaultVpnServerListUseCase,
    private val refreshVpnServers: RefreshVpnServersUseCase,
    private val getOptimalVpnServer: GetOptimalVpnServerUseCase,
    @Named(Qualifiers.APP_SCOPE) appScope: CoroutineScope,
    @Named(Qualifiers.MAIN_DISPATCHER) mainDispatcher: CoroutineDispatcher,
) : ConnectionManager,
    VpnConnectionControl {
    private val scope = CoroutineScope(appScope.coroutineContext + mainDispatcher)

    private var statusCollectionJob: Job? = null
    private var bindAttempted = false
    private var boundService: PiaService? = null

    private var serviceDeferred: CompletableDeferred<PiaService>? = null

    private var connectionJob: Job? = null

    // Guards connectionJob's cancel-then-replace. Without it, overlapping connect() calls each
    // started their own VPN session, which then fought each other over the single tun.
    private val connectionJobMutex = Mutex()

    private val connectionInProgress = AtomicBoolean(false)

    override suspend fun connect(
        server: VpnServer,
        isManual: Boolean,
        stopCallback: () -> Unit,
        showDialog: () -> Unit,
    ) {
        if (settingsPrefs.getSelectedProtocolNow() != VpnProtocols.Automatic && server.endpoints[selectedServerGroup()].isNullOrEmpty()) {
            problemDetector.onProtocolNotAvailable()
            showDialog()
            return
        }

        connectionInProgress.set(true)
        connectionInfo.updateInfo(server.name, server.iso, isManual)
        connectionPrefs.setSelectedVpnServer(server)
        connectionPrefs.addToQuickConnect(server.key, server.isDedicatedIp)

        if (!startShadowsocks(stopCallback)) {
            connectionInProgress.set(false)
            return
        }

        val job =
            connectionJobMutex.withLock {
                connectionJob?.cancelAndJoin()
                scope
                    .launch {
                        val service = startServiceIfNeeded()
                        service.startVpn(settingsPrefs.getVpnExcludedAppsNow())
                    }.also { connectionJob = it }
            }
        job.join()
    }

    override suspend fun connectToLastKnownOrOptimalServer() {
        if (!isUserLoggedIn.invoke()) return

        if (settingsPrefs.isAutomationEnabledNow() && connectionPrefs.isDisconnectedByUserNow()) {
            connectionPrefs.setDisconnectedByUser(false)
            return
        }

        val server =
            connectionPrefs.getSelectedVpnServerNow() ?: run {
                if (isDefaultVpnServerList().first()) {
                    refreshVpnServers(locale = Locale.getDefault().language, isConnected = false)
                }
                getOptimalVpnServer()
            }

        connect(server, isManual = false, stopCallback = { scope.launch { disconnect() } }, showDialog = {})
    }

    override suspend fun disconnect() {
        problemDetector.onUserInitiatedDisconnect()
        connectionJobMutex.withLock {
            connectionJob?.cancelAndJoin()
            connectionJob = null
        }

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
                statusTracker.onSdkStatusChanged(KapeVPNConnectionStatus.Disconnected)
                stopObfuscator()
                connectionSource.stopPortForwarding()
                portForwarding.clearBindPort()
                connectionInfo.resetConnectionInfo()
                connectionInProgress.set(false)
            }.join()
    }

    override suspend fun reconnect(
        server: VpnServer,
        stopCallback: () -> Unit,
    ) {
        connectionPrefs.addToQuickConnect(server.key, server.isDedicatedIp)
        disconnect()
        connect(server, isManual = true, stopCallback, showDialog = {})
    }

    override fun isConnectionInProgress(): Boolean = connectionInProgress.get()

    override fun isConnected(): Boolean = statusTracker.status.value == ConnectionStatus.CONNECTED

    private suspend fun startShadowsocks(stopCallback: () -> Unit): Boolean {
        if (!settingsPrefs.isShadowsocksObfuscationEnabledNow()) return true
        val server = shadowsocksRegionPrefs.getSelectedShadowsocksServerNow() ?: return false
        return startObfuscator(
            server =
                ObfuscatorProcessInformation(
                    serverIp = server.host,
                    serverPort = server.port.toString(),
                    serverKey = server.key,
                    serverEncryptMethod = server.cipher,
                ),
            onStopped = stopCallback,
        ).isSuccess
    }

    private suspend fun selectedServerGroup(): VpnServer.ServerGroup? =
        when (settingsPrefs.getSelectedProtocolNow()) {
            VpnProtocols.WireGuard -> VpnServer.ServerGroup.WIREGUARD
            VpnProtocols.OpenVPN ->
                when (settingsPrefs.getOpenVpnSettingsNow().transport) {
                    Transport.UDP -> VpnServer.ServerGroup.OPENVPN_UDP
                    Transport.TCP -> VpnServer.ServerGroup.OPENVPN_TCP
                    Transport.AUTO -> null
                }
            VpnProtocols.Automatic -> null
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
                        launch { service.connectionStatus.collect(statusTracker::onSdkStatusChanged) }
                        launch { service.lastTunnelError.collect(statusTracker::onTunnelError) }
                    }
            }

            override fun onServiceDisconnected(name: ComponentName) {
                boundService = null
                statusCollectionJob?.cancel()
                statusCollectionJob = null
                statusTracker.onSdkStatusChanged(KapeVPNConnectionStatus.Disconnected)
            }
        }

    private suspend fun startServiceIfNeeded(): PiaService {
        boundService?.let { return it }
        val deferred = serviceDeferred ?: CompletableDeferred<PiaService>().also { serviceDeferred = it }
        if (!bindAttempted) {
            bindAttempted = true
            ContextCompat.startForegroundService(
                context,
                Intent(context, PiaService::class.java).putExtra(PiaService.EXTRA_MANUAL_START, true),
            )
            context.bindService(Intent(context, PiaService::class.java), serviceConnection, Context.BIND_AUTO_CREATE)
        }
        return deferred.await()
    }
}