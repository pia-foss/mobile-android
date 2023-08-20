package com.kape.vpnconnect.domain

import android.app.Notification
import android.app.PendingIntent
import com.kape.utils.server.Server
import com.kape.vpnconnect.utils.ConnectionManager
import com.kape.vpnmanager.data.models.ClientConfiguration
import com.kape.vpnmanager.data.models.OpenVpnClientConfiguration
import com.kape.vpnmanager.data.models.ServerList
import com.kape.vpnmanager.data.models.WireguardClientConfiguration
import com.kape.vpnmanager.presenters.VPNManagerProtocolTarget
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.Clock
import org.koin.core.component.KoinComponent

class ConnectionUseCase(
    private val connectionSource: ConnectionDataSource,
    private val certificate: String,
    private val connectionManager: ConnectionManager
) : KoinComponent {

    fun startConnection(
        server: Server,
        configureIntent: PendingIntent,
        notification: Notification,
    ): Flow<Boolean> = flow {
        connectionManager.setConnectedServerName(server.name)
        val index = connectionSource.getVpnToken().indexOf(":")
        var transport = "udp"
        val serverGroup = when (transport) {
            "udp" -> {
                Server.ServerGroup.OPENVPN_UDP
            }

            "tcp" -> {
                Server.ServerGroup.OPENVPN_TCP
            }

            else -> {
                Server.ServerGroup.WIREGUARD
            }
        }
        val details = server.endpoints[serverGroup]
        val ip: String
        val cn: String
        val port: Int

        if (!details.isNullOrEmpty()) {
            if (details[0].ip.contains(":")) {
                ip = details[0].ip.substring(0, details[0].ip.indexOf(":"))
                port = details[0].ip.substring(details[0].ip.indexOf(":") + 1).toInt()
            } else {
                ip = details[0].ip
                port = 8080
            }
            cn = details[0].cn
        } else {
            ip = ""
            cn = ""
            port = 8080
        }

        val clientConfiguration = ClientConfiguration(
            sessionName = Clock.System.now().toString(),
            configureIntent = configureIntent,
            protocolTarget = VPNManagerProtocolTarget.OPENVPN,
            mtu = 1280,
            port = port,
            dnsList = emptyList(),
            notificationId = 123,
            notification = notification,
            allowedApplicationPackages = emptyList(),
            disallowedApplicationPackages = emptyList(),
            allowLocalNetworkAccess = false,
            serverList = ServerList(
                servers = listOf(
                    ServerList.Server(
                        ip = ip,
                        commonName = cn,
                        latency = server.latency?.toLong()
                    )
                )
            ),
            openVpnClientConfiguration = OpenVpnClientConfiguration(
                caCertificate = certificate,
                cipher = "AES-128-GCM",
                transport = transport,
                username = connectionSource.getVpnToken().substring(0, index),
                password = connectionSource.getVpnToken().substring(index + 1)
            ),
            wireguardClientConfiguration = WireguardClientConfiguration(
                token = connectionSource.getVpnToken(),
                pinningCertificate = certificate
            )
        )

        connectionSource.startConnection(clientConfiguration, connectionManager).collect {
            emit(it)
        }
    }

    fun stopConnection(): Flow<Boolean> = flow {
        connectionSource.stopConnection().collect {
            connectionManager.setConnectedServerName("")
            emit(it)
        }
    }

    fun isConnected(): Boolean = connectionManager.isConnected()
}