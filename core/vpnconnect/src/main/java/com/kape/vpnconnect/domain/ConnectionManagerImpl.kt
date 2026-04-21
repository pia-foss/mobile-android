package com.kape.vpnconnect.domain

import com.kape.contracts.ConnectionConfigurationUseCase
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
import com.kape.portforwarding.domain.PortForwardingUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.sync.Mutex
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ConnectionManagerImpl :
    ConnectionManager,
    KoinComponent {
    private val connectionSource: ConnectionDataSource by inject()
    private val connectionInfoProvider: ConnectionInfoProvider by inject()
    private val connectionPrefs: ConnectionPrefs by inject()
    private val connectionConfigurationUseCase: ConnectionConfigurationUseCase by inject()
    private val settingsPrefs: SettingsPrefs by inject()
    private val shadowsocksRegionPrefs: ShadowsocksRegionPrefs by inject()
    private val startObfuscatorProcess: StartObfuscatorProcess by inject()
    private val stopObfuscatorProcess: StopObfuscatorProcess by inject()
    private val portForwardingUseCase: PortForwardingUseCase by inject()
    private val connectionStatusProvider: ConnectionStatusProvider by inject()
    private var connectionInProgress: Boolean = false
    private var serverToConnectTo: VpnServer? = null
    private val connectionStatus = mutableListOf<Connection>()

    // Ensures only one disconnect→connect sequence runs at a time.
    private val reconnectMutex = Mutex()

    private data class Connection(
        val server: VpnServer,
        val inProgress: Boolean,
    )

    override var connectJob: Job? = null

    override suspend fun connect(
        server: VpnServer,
        isManual: Boolean,
        stopCallback: () -> Unit,
    ) {
        connectionInProgress = true
        connectionStatus.add(Connection(server, true))
        // Update connection info
        connectionInfoProvider.updateInfo(server.name, server.iso, isManual)
        connectionPrefs.setSelectedVpnServer(server)
        connectionPrefs.addToQuickConnect(server.key, server.isDedicatedIp)

        // Start Shadowsocks
        val shadowsocksOk = startShadowsocks(stopCallback)
        if (!shadowsocksOk) return

        // Start VPN
        val vpnOk =
            connectionSource
                .startConnection(
                    connectionConfigurationUseCase.generateConnectionConfiguration(
                        server,
                    ),
                    connectionStatusProvider,
                ).fold(
                    onSuccess = {
                        // Start Port Forwarding
                        startPortForwarding()
                    },
                    onFailure = {
                        disconnect().getOrNull()
                    },
                )
    }

    override suspend fun disconnect(): Result<Unit> =
        runCatching {
            stopConnection()
            stopObfuscatorProcess()
            cancelPortForwarding()
        }

    override suspend fun reconnect(
        server: VpnServer,
        stopCallback: () -> Unit,
    ) {
        // Update quick-connect history immediately so the list stays relevant
        // regardless of whether this request ends up being the one connected to.
        connectionPrefs.addToQuickConnect(server.key, server.isDedicatedIp)
        serverToConnectTo = server

        // Only one disconnect→connect sequence should run at a time. If the mutex
        // is already held another sequence is in flight; that sequence will pick up
        // the updated serverToConnectTo when it reaches the connect phase.
        if (!reconnectMutex.tryLock()) return
        try {
            disconnect().getOrNull()
            // Re-read after disconnect to pick up any server selected while disconnecting.
            // Then loop: if another server arrives during connect(), disconnect and switch to it.
            while (serverToConnectTo != null) {
                val target = serverToConnectTo!!
                serverToConnectTo = null
                connect(target, true, stopCallback)
                if (serverToConnectTo != null) disconnect().getOrNull()
            }
        } finally {
            reconnectMutex.unlock()
        }
    }

    override fun isConnectionInProgress(): Boolean = connectionInProgress

    private suspend fun startShadowsocks(stopCallback: () -> Unit): Boolean {
        if (!settingsPrefs.isShadowsocksObfuscationEnabled()) return true

        val server = shadowsocksRegionPrefs.getSelectedShadowsocksServer() ?: return false

        val result =
            startObfuscatorProcess(
                obfuscatorProcessInformation =
                    ObfuscatorProcessInformation(
                        serverIp = server.host,
                        serverPort = server.port.toString(),
                        serverKey = server.key,
                        serverEncryptMethod = server.cipher,
                    ),
                obfuscatorProcessListener =
                    object : ObfuscatorProcessListener {
                        override fun processStopped() {
                            stopCallback()
                        }
                    },
            )

        return result.isSuccess
    }

    private suspend fun startPortForwarding() {
        if (!settingsPrefs.isPortForwardingEnabled()) return
        portForwardingUseCase.bindPort(connectionSource.getVpnToken())
        connectionSource.startPortForwarding()
    }

    private suspend fun stopConnection(): Result<Unit> =
        runCatching {
            connectionSource.stopConnection().getOrThrow()
            connectionInfoProvider.resetConnectionInfo()
            connectionInProgress = false
        }

    private fun cancelPortForwarding(): Result<Unit> {
        connectionSource.stopPortForwarding()
        portForwardingUseCase.clearBindPort()
        return Result.success(Unit)
    }
}