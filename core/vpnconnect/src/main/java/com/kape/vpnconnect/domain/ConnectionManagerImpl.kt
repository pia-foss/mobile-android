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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.concurrent.atomic.AtomicBoolean

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

    // Dedicated scope for serial VPN operations (must be provided via DI)
    private val vpnScope: CoroutineScope by inject()

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

    private suspend fun handleReconnect(
        server: VpnServer,
        stopCallback: () -> Unit,
    ) {
        connectionPrefs.addToQuickConnect(server.key, server.isDedicatedIp)

        disconnect().getOrNull()

        try {
            connect(server, isManual = true, stopCallback)
        } catch (_: Exception) {
            // Swallow to keep processor alive and allow newer state to apply
        }
    }

    override suspend fun connect(
        server: VpnServer,
        isManual: Boolean,
        stopCallback: () -> Unit,
    ) {
        connectionInProgress.set(true)

        connectionInfoProvider.updateInfo(server.name, server.iso, isManual)
        connectionPrefs.setSelectedVpnServer(server)
        connectionPrefs.addToQuickConnect(server.key, server.isDedicatedIp)

        val shadowsocksOk = startShadowsocks(stopCallback)
        if (!shadowsocksOk) return

        connectionSource
            .startConnection(
                connectionConfigurationUseCase.generateConnectionConfiguration(server),
                connectionStatusProvider,
            ).fold(
                onSuccess = { startPortForwarding() },
                onFailure = { disconnect().getOrNull() },
            )
    }

    override suspend fun disconnect(): Result<Unit> =
        runCatching {
            stopConnection()
            stopObfuscatorProcess()
            cancelPortForwarding()
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
        if (!settingsPrefs.isShadowsocksObfuscationEnabled()) return true

        val server =
            shadowsocksRegionPrefs.getSelectedShadowsocksServer() ?: return false

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

    private suspend fun startPortForwarding() {
        if (!settingsPrefs.isPortForwardingEnabled()) return
        portForwardingUseCase.bindPort(connectionSource.getVpnToken())
        connectionSource.startPortForwarding()
    }

    private suspend fun stopConnection(): Result<Unit> =
        runCatching {
            connectionSource.stopConnection().getOrThrow()
            connectionInfoProvider.resetConnectionInfo()
            connectionInProgress.set(false)
        }

    private fun cancelPortForwarding(): Result<Unit> {
        connectionSource.stopPortForwarding()
        portForwardingUseCase.clearBindPort()
        return Result.success(Unit)
    }
}