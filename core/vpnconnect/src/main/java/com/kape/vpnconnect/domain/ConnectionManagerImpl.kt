package com.kape.vpnconnect.domain

import com.kape.contracts.ConnectionConfigurationUseCase
import com.kape.contracts.ConnectionInfoProvider
import com.kape.contracts.ConnectionManager
import com.kape.data.ConnectionStatus
import com.kape.data.vpnserver.VpnServer
import com.kape.localprefs.prefs.ConnectionPrefs
import com.kape.localprefs.prefs.SettingsPrefs
import com.kape.localprefs.prefs.ShadowsocksRegionPrefs
import com.kape.obfuscator.data.ObfuscatorProcessInformation
import com.kape.obfuscator.data.ObfuscatorProcessListener
import com.kape.obfuscator.domain.StartObfuscatorProcess
import com.kape.obfuscator.domain.StopObfuscatorProcess
import com.kape.portforwarding.domain.PortForwardingUseCase
import kotlinx.coroutines.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ConnectionManagerImpl : ConnectionManager, KoinComponent {

    private val connectionSource: ConnectionDataSource by inject()
    private val connectionInfoProvider: ConnectionInfoProvider by inject()
    private val connectionPrefs: ConnectionPrefs by inject()
    private val connectionConfigurationUseCase: ConnectionConfigurationUseCase by inject()
    private val settingsPrefs: SettingsPrefs by inject()
    private val shadowsocksRegionPrefs: ShadowsocksRegionPrefs by inject()
    private val startObfuscatorProcess: StartObfuscatorProcess by inject()
    private val stopObfuscatorProcess: StopObfuscatorProcess by inject()
    private val portForwardingUseCase: PortForwardingUseCase by inject()

    // Single scope for all connections
    private val managerScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Job representing the current active connection flow
    private var connectJob: Job? = null

    override fun connectionInProgress() = connectJob?.isActive == true

    @Volatile
    private var isDisconnecting = false

    init {
        // Stop any library-initiated reconnect while we're in disconnected state
        managerScope.launch {
            connectionInfoProvider.connectionState.collect { vpnStatus ->
                if (isDisconnecting && (vpnStatus.status == ConnectionStatus.CONNECTING || vpnStatus.status == ConnectionStatus.CONNECTED)) {
                    println("--- manager: ${vpnStatus.status}")
                    connectionSource.stopConnection()
                    connectionInfoProvider.resetConnectionInfo()
                }
            }
        }
    }

    // -------------------- Public API --------------------

    override suspend fun connect(server: VpnServer, isManual: Boolean) {
        isDisconnecting = false
        connectJob?.cancelAndJoin()

        connectJob = managerScope.launch {
            connectInternal(server, isManual, this)
        }
        connectJob?.join()
    }

    override suspend fun disconnect() {
        isDisconnecting = true
        connectJob?.cancelAndJoin()
        connectJob = null
        cleanup()
    }

    override suspend fun reconnect(server: VpnServer) {
        disconnect()
        connect(server, true)
    }

    // -------------------- Internal helpers --------------------

    private suspend fun connectInternal(server: VpnServer, isManual: Boolean, scope: CoroutineScope) {
        scope.coroutineContext.ensureActive()

        // Update connection info
        connectionInfoProvider.updateInfo(server.name, server.iso, isManual)
        connectionPrefs.setSelectedVpnServer(server)
        connectionPrefs.addToQuickConnect(server.key, server.isDedicatedIp)

        scope.coroutineContext.ensureActive()

        // Start Shadowsocks
        val shadowsocksOk = startShadowsocks(scope)
        if (!shadowsocksOk) return
        scope.coroutineContext.ensureActive()

        // Start VPN
        val vpnOk = startVpnConnection(server, scope)
        if (!vpnOk) {
            stopShadowsocks()
            return
        }

        scope.coroutineContext.ensureActive()

        // Start Port Forwarding
        startPortForwarding(scope)
    }

    private suspend fun startVpnConnection(server: VpnServer, scope: CoroutineScope): Boolean {
        scope.coroutineContext.ensureActive()
        val connected = withContext(Dispatchers.IO) {
            connectionSource.startConnection(
                connectionConfigurationUseCase.generateConnectionConfiguration(
                    server,
                ),
                scope,
            )
        }
        scope.coroutineContext.ensureActive()
        return connected
    }

    private suspend fun startShadowsocks(scope: CoroutineScope): Boolean {
        if (!settingsPrefs.isShadowsocksObfuscationEnabled()) return true

        val server = shadowsocksRegionPrefs.getSelectedShadowsocksServer() ?: return false

        scope.coroutineContext.ensureActive()

        val result = startObfuscatorProcess(
            obfuscatorProcessInformation = ObfuscatorProcessInformation(
                serverIp = server.host,
                serverPort = server.port.toString(),
                serverKey = server.key,
                serverEncryptMethod = server.cipher,
            ),
            obfuscatorProcessListener = object : ObfuscatorProcessListener {
                override fun processStopped() {
                    // Only cleanup if this connection flow is cancelled
                    if (!scope.isActive) return
                    scope.launch { stopConnection() }
                }
            },
        )

        if (!scope.isActive) stopObfuscatorProcess()
        return result.isSuccess
    }

    private suspend fun startPortForwarding(scope: CoroutineScope) {
        if (!settingsPrefs.isPortForwardingEnabled() || !scope.isActive) return
        portForwardingUseCase.bindPort(connectionSource.getVpnToken())
        connectionSource.startPortForwarding()
    }

    private suspend fun stopShadowsocks() = stopObfuscatorProcess()
    private suspend fun stopConnection() {
        connectionSource.stopConnection()
        connectionInfoProvider.resetConnectionInfo()
    }

    private fun cancelPortForwarding() {
        connectionSource.stopPortForwarding()
        portForwardingUseCase.clearBindPort()
    }

    private suspend fun cleanup() {
        stopConnection()
        stopShadowsocks()
        cancelPortForwarding()
    }
}