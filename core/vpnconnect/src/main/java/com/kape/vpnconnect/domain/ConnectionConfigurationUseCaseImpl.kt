package com.kape.vpnconnect.domain

import android.app.Notification
import android.app.PendingIntent
import com.kape.localprefs.prefs.ConnectionPrefs
import com.kape.localprefs.prefs.SettingsPrefs
import com.kape.localprefs.prefs.ShadowsocksRegionPrefs
import com.kape.obfuscator.domain.StartObfuscatorProcess.Companion.OBFUSCATOR_PROXY_HOST
import com.kape.obfuscator.domain.StartObfuscatorProcess.Companion.OBFUSCATOR_PROXY_PORT
import com.kape.settings.data.DataEncryption
import com.kape.settings.data.DnsOptions
import com.kape.settings.data.ProtocolSettings
import com.kape.settings.data.Transport
import com.kape.settings.data.VpnProtocols
import com.kape.utils.vpnserver.VpnServer
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
import kotlinx.datetime.Clock
import org.koin.core.annotation.Singleton
import org.koin.core.component.KoinComponent
import kotlin.collections.isNullOrEmpty

private const val MACE_DNS = "10.0.0.241"
private const val PIA_DNS = "10.0.0.243"

@Singleton([ConnectionConfigurationUseCase::class])
class ConnectionConfigurationUseCaseImpl(
    private val connectionSource: ConnectionDataSource,
    private val certificate: String,
    private val settingsPrefs: SettingsPrefs,
    private val connectionPrefs: ConnectionPrefs,
    private val shadowsocksRegionPrefs: ShadowsocksRegionPrefs,
    private val getActiveInterfaceDnsUseCase: GetActiveInterfaceDnsUseCase,
    private val notificationBuilder: Notification.Builder,
    private val configureIntent: PendingIntent,
    private val automationPendingIntent: PendingIntent,
) : ConnectionConfigurationUseCase, KoinComponent {

    override fun generateConnectionConfiguration(server: VpnServer): ClientConfiguration {
        val cipher = when (settingsPrefs.getOpenVpnSettings().dataEncryption) {
            DataEncryption.AES_128_GCM -> "AES-128-GCM"
            DataEncryption.AES_256_GCM -> "AES-256-GCM"
            DataEncryption.CHA_CHA_20 -> "CHACHA20-POLY1305"
        }

        var additionalOpenVpnParams = "--cipher $cipher "
        if (server.isDedicatedIp) {
            additionalOpenVpnParams += "--ncp-disable --pia-signal-settings"
        }

        // block ipv6
        additionalOpenVpnParams += "--block-ipv6"

        notificationBuilder.setContentTitle("${server.name} - privateinternetaccess.com")
        if (settingsPrefs.isAutomationEnabled()) {
            notificationBuilder.setContentIntent(automationPendingIntent)
        } else {
            notificationBuilder.setContentIntent(configureIntent)
        }

        return ClientConfiguration(
            sessionName = Clock.System.now().toString(),
            configureIntent = configureIntent,
            protocolTarget = getProtocolInfo().first,
            mtu = getProtocolInfo().second.mtu,
            notificationId = NOTIFICATION_ID,
            notification = notificationBuilder.build(),
            allowedApplicationPackages = emptyList(),
            disallowedApplicationPackages = settingsPrefs.getVpnExcludedApps(),
            allowLocalNetworkAccess = settingsPrefs.isAllowLocalTrafficEnabled(),
            serverList = ServerList(servers = getEndpoints(server)),
            openVpnClientConfiguration = OpenVpnClientConfiguration(
                caCertificate = certificate,
                username = getUsernameAndPassword().first,
                password = getUsernameAndPassword().second,
                socksProxy = getProxyDetails(),
                additionalParameters = additionalOpenVpnParams,
            ),
            wireguardClientConfiguration = WireguardClientConfiguration(
                token = connectionSource.getVpnToken(),
                pinningCertificate = certificate,
            ),
        )
    }

    override suspend fun updateServerConfig(server: VpnServer): Boolean =
        connectionSource.updateConfigurationServers(ServerList(getEndpoints(server)))

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

    private fun getUsernameAndPassword(): Pair<String, String> {
        var username = ""
        var password = ""

        connectionSource.getVpnToken().indexOf(":").let { index ->
            if (index != -1) {
                username = connectionSource.getVpnToken().substring(0, index)
                password = connectionSource.getVpnToken().substring(index + 1)
            }
        }
        return Pair(username, password)
    }

    private fun getDnsList(): List<String> {
        return if (settingsPrefs.isMaceEnabled()) {
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
    }

    private fun getProtocolInfo(): Pair<VPNManagerProtocolTarget, ProtocolSettings> {
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
        return Pair(protocolTarget, settings)
    }

    private fun getEndpoints(server: VpnServer): List<ServerList.Server> {
        val details = server.endpoints[getServerGroup()]
        val serverList = mutableListOf<ServerList.Server>()
        if (details.isNullOrEmpty()) {
            serverList.add(createServer("", "", 8080, server, emptyList()))
        } else {
            for (endpoint in details) {
                if (endpoint.ip.contains(":")) {
                    val ip = endpoint.ip.substring(0, endpoint.ip.indexOf(":"))
                    val port = endpoint.ip.substring(endpoint.ip.indexOf(":") + 1).toInt()
                    serverList.add(createServer(ip, endpoint.cn, port, server, getDnsList()))
                } else {
                    val ip = endpoint.ip
                    val port = settingsPrefs.getOpenVpnSettings().port.toInt()
                    serverList.add(createServer(ip, endpoint.cn, port, server, getDnsList()))
                }
            }
        }
        return serverList.toList()
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

    private fun createServer(
        ip: String,
        cn: String,
        port: Int,
        server: VpnServer,
        dnsList: List<String>,
    ): ServerList.Server {
        return ServerList.Server(
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
        )
    }
}