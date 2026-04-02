package com.kape.vpnconnect.domain

import android.app.Notification
import android.app.PendingIntent
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.kape.localprefs.prefs.ConnectionPrefs
import com.kape.localprefs.prefs.NO_IP
import com.kape.localprefs.prefs.SettingsPrefs
import com.kape.localprefs.prefs.ShadowsocksRegionPrefs
import com.kape.obfuscator.data.ObfuscatorProcessInformation
import com.kape.obfuscator.data.ObfuscatorProcessListener
import com.kape.obfuscator.domain.StartObfuscatorProcess
import com.kape.obfuscator.domain.StartObfuscatorProcess.Companion.OBFUSCATOR_PROXY_HOST
import com.kape.obfuscator.domain.StartObfuscatorProcess.Companion.OBFUSCATOR_PROXY_PORT
import com.kape.obfuscator.domain.StopObfuscatorProcess
import com.kape.portforwarding.data.model.PortForwardingStatus
import com.kape.portforwarding.domain.PortForwardingUseCase
import com.kape.utils.vpnserver.VpnServer
import com.kape.vpnconnect.utils.ConnectionManager
import com.kape.vpnconnect.utils.ConnectionStatus
import com.kape.vpnconnect.utils.NOTIFICATION_ID
import com.kape.vpnmanager.api.OpenVpnSocksProxyDetails
import com.kape.vpnmanager.data.models.ClientConfiguration
import com.kape.vpnmanager.data.models.DnsInformation
import com.kape.vpnmanager.data.models.OpenVpnClientConfiguration
import com.kape.vpnmanager.data.models.ProtocolCipher
import com.kape.vpnmanager.data.models.ServerList
import com.kape.vpnmanager.data.models.TransportProtocol
import com.kape.vpnmanager.data.models.WireguardClientConfiguration
import com.kape.vpnmanager.presenters.VPNManagerProtocolTarget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import org.koin.core.annotation.Singleton
import org.koin.core.component.KoinComponent

private const val MACE_DNS = "10.0.0.241"
private const val PIA_DNS = "10.0.0.243"

@Singleton(binds = [ConnectionUseCase::class])
class ConnectionUseCaseImpl(
    private val connectionSource: ConnectionDataSource,
    private val clientStateDataSource: ClientStateDataSource,
    private val connectionManager: ConnectionManager,
    private val connectionPrefs: ConnectionPrefs,
    private val settingsPrefs: SettingsPrefs,
    private val shadowsocksRegionPrefs: ShadowsocksRegionPrefs,
    private val startObfuscatorProcess: StartObfuscatorProcess,
    private val stopObfuscatorProcess: StopObfuscatorProcess,
    private val portForwardingUseCase: PortForwardingUseCase,
    private val connectionConfigurationUseCase: ConnectionConfigurationUseCase,
) : ConnectionUseCase, KoinComponent {
    override val portForwardingStatus = portForwardingUseCase.portForwardingStatus
    override val port = portForwardingUseCase.port
    override val clientIp = MutableStateFlow(connectionPrefs.getClientIp())
    override val vpnIp = MutableStateFlow(connectionPrefs.getVpnIp())

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override suspend fun startConnection(server: VpnServer, isManualConnection: Boolean): Boolean {
        connectionManager.setConnectedServerName(server.name, server.iso)
        connectionManager.isManualConnection = isManualConnection
        connectionPrefs.setSelectedVpnServer(server)
        val succeeded = startShadowsocksConnection()
        if (!succeeded) return false
        val connected = startVpnConnection(server)
        if (!connected) stopShadowsocksConnection()
        return connected
    }

    override suspend fun stopConnection(): Boolean {
        val result = stopVpnConnection()
        stopShadowsocksConnection()
        stopPortForwarding()
        return result
    }

    override suspend fun reconnect(server: VpnServer): Boolean {
        if (isConnected()) {
            stopConnection()
        }
        return startConnection(server, false)
    }

    override fun isConnected(): Boolean = connectionManager.isConnected()

    override fun isConnecting(): Boolean = connectionManager.isConnecting()

    override fun isNotDisconnected(): Boolean = connectionManager.isNotDisconnected()

    override suspend fun getClientStatus(status: ConnectionStatus): ConnectionStatus {
        clientStateDataSource.getClientStatus(status)
        clientIp.update { connectionPrefs.getClientIp() }
        vpnIp.update { connectionPrefs.getVpnIp() }
        return status
    }

    override fun getConnectionStatus() = connectionManager.connectionStatus

    override fun resetVpnIp(): StateFlow<String> {
        vpnIp.update { NO_IP }
        return vpnIp
    }

    private fun getVpnToken() = connectionSource.getVpnToken()

    private suspend fun startVpnConnection(server: VpnServer): Boolean {
        val connected = connectionSource.startConnection(
            connectionConfigurationUseCase.generateConnectionConfiguration(server = server),
            connectionManager,
        )
        startPortForwarding()
        return connected
    }

    private suspend fun stopVpnConnection(): Boolean {
        val result = connectionSource.stopConnection()
        connectionManager.setConnectedServerName("", "")
        clientStateDataSource.resetVpnIp()
        vpnIp.value = connectionPrefs.getVpnIp()
        stopPortForwarding()
        return result
    }

    private suspend fun startShadowsocksConnection(): Boolean {
        if (settingsPrefs.isShadowsocksObfuscationEnabled().not()) return true

        val selectedShadowsocksServer =
            shadowsocksRegionPrefs.getSelectedShadowsocksServer() ?: return false

        val result = startObfuscatorProcess(
            obfuscatorProcessInformation = ObfuscatorProcessInformation(
                serverIp = selectedShadowsocksServer.host,
                serverPort = selectedShadowsocksServer.port.toString(),
                serverKey = selectedShadowsocksServer.key,
                serverEncryptMethod = selectedShadowsocksServer.cipher,
            ),
            obfuscatorProcessListener = object : ObfuscatorProcessListener {
                override fun processStopped() {
                    scope.launch { stopConnection() }
                }
            },
        )
        return result.isSuccess
    }

    private suspend fun stopShadowsocksConnection(): Boolean {
        return stopObfuscatorProcess().isSuccess
    }

    private suspend fun startPortForwarding(): Boolean {
        return if (settingsPrefs.isPortForwardingEnabled()) {
            portForwardingUseCase.bindPort(getVpnToken())
            connectionSource.startPortForwarding()
            true
        } else {
            false
        }
    }

    private fun stopPortForwarding() {
        connectionSource.stopPortForwarding()
        portForwardingUseCase.clearBindPort()
    }
}