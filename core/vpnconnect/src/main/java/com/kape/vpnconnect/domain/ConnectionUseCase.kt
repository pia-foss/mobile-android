package com.kape.vpnconnect.domain

import android.app.Notification
import android.app.PendingIntent
import com.kape.connection.ConnectionPrefs
import com.kape.settings.SettingsPrefs
import com.kape.settings.data.DnsOptions
import com.kape.settings.data.ProtocolSettings
import com.kape.settings.data.Transport
import com.kape.settings.data.VpnProtocols
import com.kape.utils.vpnserver.VpnServer
import com.kape.vpnconnect.utils.ConnectionManager
import com.kape.vpnmanager.data.models.ClientConfiguration
import com.kape.vpnmanager.data.models.DnsInformation
import com.kape.vpnmanager.data.models.OpenVpnClientConfiguration
import com.kape.vpnmanager.data.models.ServerList
import com.kape.vpnmanager.data.models.WireguardClientConfiguration
import com.kape.vpnmanager.presenters.VPNManagerProtocolTarget
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.Clock
import org.koin.core.component.KoinComponent

private const val MACE_DNS = "10.0.0.241"

class ConnectionUseCase(
    private val connectionSource: ConnectionDataSource,
    private val certificate: String,
    private val connectionManager: ConnectionManager,
    private val settingsPrefs: SettingsPrefs,
    private val connectionPrefs: ConnectionPrefs,
    private val configureIntent: PendingIntent,
    private val notificationBuilder: Notification.Builder,
    private val getActiveInterfaceDnsUseCase: GetActiveInterfaceDnsUseCase,
) : KoinComponent {

    companion object {
        private const val PIA_DNS = "10.0.0.242"
    }

    fun startConnection(server: VpnServer, isManualConnection: Boolean): Flow<Boolean> = flow {
        connectionManager.setConnectedServerName(server.name, server.iso)
        connectionManager.isManualConnection = isManualConnection
        connectionPrefs.setSelectedServer(server)
        val index = connectionSource.getVpnToken().indexOf(":")
        val details = server.endpoints[getServerGroup()]

        val ip: String
        val cn: String
        val port: Int

        if (!details.isNullOrEmpty()) {
            if (details[0].ip.contains(":")) {
                ip = details[0].ip.substring(0, details[0].ip.indexOf(":"))
                port = details[0].ip.substring(details[0].ip.indexOf(":") + 1).toInt()
            } else {
                ip = details[0].ip
                port = settingsPrefs.getOpenVpnSettings().port.toInt()
            }
            cn = details[0].cn
        } else {
            ip = ""
            cn = ""
            port = 8080
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
                    emptyList()
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
        notificationBuilder.setContentText("Connected")
        notificationBuilder.setContentIntent(configureIntent)

        val clientConfiguration = ClientConfiguration(
            sessionName = Clock.System.now().toString(),
            configureIntent = configureIntent,
            protocolTarget = protocolTarget,
            mtu = settings.mtu,
            port = port,
            dnsInformation = DnsInformation(
                dnsList = dnsList,
                systemDnsResolverEnabled = settingsPrefs.getSelectedDnsOption() == DnsOptions.SYSTEM,
            ),
            notificationId = 123,
            notification = notificationBuilder.build(),
            allowedApplicationPackages = emptyList(),
            disallowedApplicationPackages = settingsPrefs.getVpnExcludedApps(),
            allowLocalNetworkAccess = settingsPrefs.isAllowLocalTrafficEnabled(),
            serverList = ServerList(
                servers = listOf(
                    ServerList.Server(
                        ip = ip,
                        commonName = cn,
                        latency = server.latency?.toLong(),
                    ),
                ),
            ),
            openVpnClientConfiguration = OpenVpnClientConfiguration(
                caCertificate = certificate,
                cipher = settingsPrefs.getOpenVpnSettings().dataEncryption.value,
                transport = settingsPrefs.getOpenVpnSettings().transport.value.lowercase(),
                username = connectionSource.getVpnToken().substring(0, index),
                password = connectionSource.getVpnToken().substring(index + 1),
            ),
            wireguardClientConfiguration = WireguardClientConfiguration(
                token = connectionSource.getVpnToken(),
                pinningCertificate = certificate,
            ),
        )

        connectionSource.startConnection(clientConfiguration, connectionManager).collect {
            emit(it)
        }
    }

    fun stopConnection(): Flow<Boolean> = flow {
        connectionSource.stopConnection().collect {
            connectionManager.setConnectedServerName("", "")
            emit(it)
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

    fun getServerGroup(): VpnServer.ServerGroup = when (settingsPrefs.getSelectedProtocol()) {
        VpnProtocols.WireGuard -> VpnServer.ServerGroup.WIREGUARD
        VpnProtocols.OpenVPN -> {
            if (settingsPrefs.getOpenVpnSettings().transport == Transport.UDP) {
                VpnServer.ServerGroup.OPENVPN_UDP
            } else {
                VpnServer.ServerGroup.OPENVPN_TCP
            }
        }
    }

    fun getVpnToken() = connectionSource.getVpnToken()
}