package com.kape.vpnconnect.domain

import android.app.Notification
import android.app.PendingIntent
import androidx.compose.runtime.mutableStateOf
import com.kape.connection.ConnectionPrefs
import com.kape.obfuscator.data.ObfuscatorProcessInformation
import com.kape.obfuscator.data.ObfuscatorProcessListener
import com.kape.obfuscator.domain.StartObfuscatorProcess
import com.kape.obfuscator.domain.StartObfuscatorProcess.Companion.OBFUSCATOR_PROXY_HOST
import com.kape.obfuscator.domain.StartObfuscatorProcess.Companion.OBFUSCATOR_PROXY_PORT
import com.kape.obfuscator.domain.StopObfuscatorProcess
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.Clock
import org.koin.core.component.KoinComponent

private const val MACE_DNS = "10.0.0.241"
private const val PIA_DNS = "10.0.0.242"

class ConnectionUseCase(
    private val connectionSource: ConnectionDataSource,
    private val clientStateDataSource: ClientStateDataSource,
    private val certificate: String,
    private val connectionManager: ConnectionManager,
    private val settingsPrefs: SettingsPrefs,
    private val connectionPrefs: ConnectionPrefs,
    private val shadowsocksRegionPrefs: ShadowsocksRegionPrefs,
    private val configureIntent: PendingIntent,
    private val notificationBuilder: Notification.Builder,
    private val getActiveInterfaceDnsUseCase: GetActiveInterfaceDnsUseCase,
    private val startObfuscatorProcess: StartObfuscatorProcess,
    private val stopObfuscatorProcess: StopObfuscatorProcess,
    private val portForwardingUseCase: PortForwardingUseCase,
) : KoinComponent {

    val portForwardingStatus = portForwardingUseCase.portForwardingStatus
    val port = portForwardingUseCase.port
    val clientIp = mutableStateOf(connectionPrefs.getClientIp())
    val vpnIp = mutableStateOf(connectionPrefs.getVpnIp())

    fun startConnection(server: VpnServer, isManualConnection: Boolean): Flow<Boolean> = flow {
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

    fun stopConnection(): Flow<Boolean> = flow {
        stopVpnConnection().collect {
            stopShadowsocksConnection().collect {
                stopPortForwarding()
                emit(it)
            }
        }
    }

    fun reconnect(server: VpnServer): Flow<Boolean> = flow {
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

    fun isConnected(): Boolean = connectionManager.isConnected()

    fun isConnecting(): Boolean = connectionManager.isConnecting()

    private fun getVpnToken() = connectionSource.getVpnToken()

    private fun startVpnConnection(server: VpnServer): Flow<Boolean> = flow {
        connectionSource.startConnection(
            generateConnectionConfiguration(server = server),
            connectionManager,
        ).collect { connected ->
            emit(connected)
            // The API can be sometimes be called before the tunnel is up which means the request times out
            // Add a delay prior to the request to avoid it.
            delay(1000)
            clientStateDataSource.getClientStatus().collect {
                if (!connected) {
                    clientIp.value = connectionPrefs.getClientIp()
                    clientStateDataSource.resetVpnIp()
                }
                vpnIp.value = connectionPrefs.getVpnIp()
                startPortForwarding().collect()
            }
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

    fun getClientStatus(): Flow<Boolean> = flow {
        clientStateDataSource.getClientStatus().collect {
            clientIp.value = connectionPrefs.getClientIp()
            vpnIp.value = connectionPrefs.getVpnIp()
        }
    }

    private fun getServerGroup(): VpnServer.ServerGroup =
        when (settingsPrefs.getSelectedProtocol()) {
            VpnProtocols.WireGuard -> VpnServer.ServerGroup.WIREGUARD
            VpnProtocols.OpenVPN -> {
                if (settingsPrefs.getOpenVpnSettings().transport == Transport.UDP) {
                    VpnServer.ServerGroup.OPENVPN_UDP
                } else {
                    VpnServer.ServerGroup.OPENVPN_TCP
                }
            }
        }

    private fun generateConnectionConfiguration(server: VpnServer): ClientConfiguration {
        var username = ""
        var password = ""

        connectionSource.getVpnToken().indexOf(":").let {index ->
            if (index != -1) {
                username = connectionSource.getVpnToken().substring(0, index)
                password = connectionSource.getVpnToken().substring(index + 1)
            }
        }

        val details = server.endpoints[getServerGroup()]

        val ip: String
        val cn: String
        val port: Int

        if (details.isNullOrEmpty()) {
            ip = ""
            cn = ""
            port = 8080
        } else {
            if (details[0].ip.contains(":")) {
                ip = details[0].ip.substring(0, details[0].ip.indexOf(":"))
                port = details[0].ip.substring(details[0].ip.indexOf(":") + 1).toInt()
            } else {
                ip = details[0].ip
                port = settingsPrefs.getOpenVpnSettings().port.toInt()
            }
            cn = details[0].cn
        }

        val protocolTarget: VPNManagerProtocolTarget
        val settings: ProtocolSettings
        when (settingsPrefs.getSelectedProtocol()) {
            VpnProtocols.WireGuard -> {
                protocolTarget = VPNManagerProtocolTarget.WIREGUARD
                settings = settingsPrefs.getWireGuardSettings()
            }

            VpnProtocols.OpenVPN -> {
                protocolTarget = VPNManagerProtocolTarget.OPENVPN
                settings = settingsPrefs.getOpenVpnSettings()
            }
        }

        val dnsList = if (settingsPrefs.isMaceEnabled()) {
            listOf(MACE_DNS)
        } else {
            when (settingsPrefs.getSelectedDnsOption()) {
                DnsOptions.PIA -> {
                    listOf(PIA_DNS)
                }

                DnsOptions.SYSTEM -> {
                    getActiveInterfaceDnsUseCase.invoke()
                }

                DnsOptions.CUSTOM -> {
                    val result = mutableListOf<String>()
                    val customDns = settingsPrefs.getCustomDns()
                    if (customDns.primaryDns.isNotEmpty()) {
                        result.add(customDns.primaryDns)
                    }
                    if (customDns.secondaryDns.isNotEmpty()) {
                        result.add(customDns.secondaryDns)
                    }
                    result
                }
            }
        }

        notificationBuilder.setContentTitle("${server.name} - privateinternetaccess.com")
        notificationBuilder.setContentIntent(configureIntent)

        return ClientConfiguration(
            sessionName = Clock.System.now().toString(),
            configureIntent = configureIntent,
            protocolTarget = protocolTarget,
            mtu = settings.mtu,
            notificationId = NOTIFICATION_ID,
            notification = notificationBuilder.build(),
            allowedApplicationPackages = emptyList(),
            disallowedApplicationPackages = settingsPrefs.getVpnExcludedApps(),
            allowLocalNetworkAccess = settingsPrefs.isAllowLocalTrafficEnabled(),
            serverList = ServerList(
                servers = listOf(
                    ServerList.Server(
                        ip = ip,
                        port = port,
                        commonOrDistinguishedName = cn,
                        transport = when (settingsPrefs.getOpenVpnSettings().transport) {
                            Transport.UDP -> TransportProtocol.UDP
                            Transport.TCP -> TransportProtocol.TCP
                        },
                        ciphers = when (settingsPrefs.getOpenVpnSettings().dataEncryption) {
                            DataEncryption.AES_128_GCM -> listOf(ProtocolCipher.AES_128_GCM)
                            DataEncryption.AES_256_GCM -> listOf(ProtocolCipher.AES_256_GCM)
                            DataEncryption.CHA_CHA_20 -> listOf(ProtocolCipher.CHA_CHA_20)
                        },
                        latency = server.latency?.toLong(),
                        dnsInformation = DnsInformation(
                            dnsList = dnsList,
                            systemDnsResolverEnabled = settingsPrefs.getSelectedDnsOption() == DnsOptions.SYSTEM,
                        ),
                    ),
                ),
            ),
            openVpnClientConfiguration = OpenVpnClientConfiguration(
                caCertificate = certificate,
                username = username,
                password = password,
                socksProxy = getProxyDetails(),
            ),
            wireguardClientConfiguration = WireguardClientConfiguration(
                token = connectionSource.getVpnToken(),
                pinningCertificate = certificate,
            ),
        )
    }

    private fun getProxyDetails(): OpenVpnSocksProxyDetails? {
        var proxyDetails: OpenVpnSocksProxyDetails? = null
        if (settingsPrefs.isShadowsocksObfuscationEnabled()) {
            proxyDetails = shadowsocksRegionPrefs.getSelectedShadowsocksServer()?.let {
                OpenVpnSocksProxyDetails(
                    clientProxyAddress = OBFUSCATOR_PROXY_HOST,
                    clientProxyPort = OBFUSCATOR_PROXY_PORT,
                    serverProxyAddress = it.host,
                )
            }
        }
        if (settingsPrefs.isExternalProxyAppEnabled()) {
            proxyDetails = connectionPrefs.getSelectedVpnServer()?.let {
                OpenVpnSocksProxyDetails(
                    clientProxyAddress = OBFUSCATOR_PROXY_HOST,
                    clientProxyPort = connectionPrefs.getProxyPort().toInt(),
                    serverProxyAddress = OBFUSCATOR_PROXY_HOST,
                )
            }
        }
        return proxyDetails
    }
}