package com.kape.vpnconnect.domain

import android.app.Notification
import android.app.PendingIntent
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.kape.connection.ConnectionPrefs
import com.kape.connection.NO_IP
import com.kape.obfuscator.data.ObfuscatorProcessInformation
import com.kape.obfuscator.data.ObfuscatorProcessListener
import com.kape.obfuscator.domain.StartObfuscatorProcess
import com.kape.obfuscator.domain.StartObfuscatorProcess.Companion.OBFUSCATOR_PROXY_HOST
import com.kape.obfuscator.domain.StartObfuscatorProcess.Companion.OBFUSCATOR_PROXY_PORT
import com.kape.obfuscator.domain.StopObfuscatorProcess
import com.kape.portforwarding.data.model.PortForwardingStatus
import com.kape.portforwarding.domain.PortForwardingUseCase
import com.kape.settings.SettingsPrefs
import com.kape.settings.data.DataEncryption
import com.kape.settings.data.DnsOptions
import com.kape.settings.data.ProtocolSettings
import com.kape.settings.data.Transport
import com.kape.settings.data.VpnProtocols
import com.kape.shadowsocksregions.ShadowsocksRegionPrefs
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.datetime.Clock
import org.koin.core.component.KoinComponent

private const val MACE_DNS = "10.0.0.241"
private const val PIA_DNS = "10.0.0.243"

internal class ConnectionUseCaseImpl(
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
    override fun startConnection(server: VpnServer, isManualConnection: Boolean): Flow<Boolean> =
        flow {
            connectionManager.setConnectedServerName(server.name, server.iso)
            connectionManager.isManualConnection = isManualConnection
            connectionPrefs.setSelectedVpnServer(server)
            startShadowsocksConnection().collect { succeeded ->
                if (succeeded.not()) {
                    emit(false)
                    return@collect
                }

                startVpnConnection(server = server).collect { connected ->
                    emit(connected)
                    if (!connected) {
                        stopShadowsocksConnection().collect()
                    }
                }
            }
        }

    override fun stopConnection(): Flow<Boolean> = flow {
        stopVpnConnection().collect {
            stopShadowsocksConnection().collect {
                stopPortForwarding()
                emit(it)
            }
        }
    }

    override fun reconnect(server: VpnServer): Flow<Boolean> = flow {
        if (isConnected()) {
            stopConnection().collect {
                startConnection(server, false).collect {
                    emit(it)
                }
            }
        } else {
            startConnection(server, false).collect {
                emit(it)
            }
        }
    }

    override fun isConnected(): Boolean = connectionManager.isConnected()

    override fun isConnecting(): Boolean = connectionManager.isConnecting()

    override fun isNotDisconnected(): Boolean = connectionManager.isNotDisconnected()

    override fun getClientStatus(status: ConnectionStatus): Flow<ConnectionStatus> = flow {
        clientStateDataSource.getClientStatus(status).collect {
            clientIp.update { connectionPrefs.getClientIp() }
            vpnIp.update { connectionPrefs.getVpnIp() }
            emit(status)
        }
    }

    override fun getConnectionStatus() = connectionManager.connectionStatus

    override fun resetVpnIp(): StateFlow<String> {
        vpnIp.update { NO_IP }
        return vpnIp
    }

    private fun getVpnToken() = connectionSource.getVpnToken()

    private fun startVpnConnection(server: VpnServer): Flow<Boolean> = flow {
        connectionSource.startConnection(
            connectionConfigurationUseCase.generateConnectionConfiguration(server = server),
            connectionManager,
        ).collect { connected ->
            startPortForwarding().collect()
            emit(connected)
        }
    }

    private fun stopVpnConnection(): Flow<Boolean> = flow {
        connectionSource.stopConnection().collect {
            connectionManager.setConnectedServerName("", "")
            clientStateDataSource.resetVpnIp()
            vpnIp.value = connectionPrefs.getVpnIp()
            stopPortForwarding()
            emit(it)
        }
    }

    private fun startShadowsocksConnection(): Flow<Boolean> = flow {
        if (settingsPrefs.isShadowsocksObfuscationEnabled().not()) {
            emit(true)
            return@flow
        }

        val selectedShadowsocksServer =
            shadowsocksRegionPrefs.getSelectedShadowsocksServer() ?: run {
                emit(false)
                return@flow
            }

        startObfuscatorProcess(
            obfuscatorProcessInformation = ObfuscatorProcessInformation(
                serverIp = selectedShadowsocksServer.host,
                serverPort = selectedShadowsocksServer.port.toString(),
                serverKey = selectedShadowsocksServer.key,
                serverEncryptMethod = selectedShadowsocksServer.cipher,
            ),
            obfuscatorProcessListener = object : ObfuscatorProcessListener {
                override fun processStopped() {
                    stopConnection()
                }
            },
        ).collect {
            it.fold(
                onSuccess = {
                    emit(true)
                },
                onFailure = {
                    emit(false)
                },
            )
        }
    }

    private fun stopShadowsocksConnection(): Flow<Boolean> = flow {
        stopObfuscatorProcess().collect {
            it.fold(
                onSuccess = {
                    emit(true)
                },
                onFailure = {
                    emit(false)
                },
            )
        }
    }

    private fun startPortForwarding(): Flow<Boolean> = flow {
        if (settingsPrefs.isPortForwardingEnabled()) {
            portForwardingUseCase.bindPort(getVpnToken())
            connectionSource.startPortForwarding()
            emit(true)
        } else {
            emit(false)
        }
    }

    private fun stopPortForwarding() {
        connectionSource.stopPortForwarding()
        portForwardingUseCase.clearBindPort()
    }
}