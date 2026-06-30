package com.kape.vpnconnect.domain

import android.app.Notification
import android.app.PendingIntent
import com.kape.contracts.ConnectionConfigurationUseCase
import com.kape.data.NOTIFICATION_ID
import com.kape.data.shadowsocksserver.ShadowsocksServer
import com.kape.data.vpnserver.VpnServer
import com.kape.localprefs.prefs.ConnectionPrefs
import com.kape.localprefs.prefs.SettingsPrefs
import com.kape.localprefs.prefs.ShadowsocksRegionPrefs
import com.kape.obfuscator.domain.StartObfuscatorProcess.Companion.OBFUSCATOR_PROXY_HOST
import com.kape.obfuscator.domain.StartObfuscatorProcess.Companion.OBFUSCATOR_PROXY_PORT
import com.kape.settings.data.CustomDns
import com.kape.settings.data.DataEncryption
import com.kape.settings.data.DnsOptions
import com.kape.settings.data.Transport
import com.kape.settings.data.VpnProtocols
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
) : ConnectionConfigurationUseCase,
    KoinComponent {
    override suspend fun generateConnectionConfiguration(server: VpnServer): ClientConfiguration {
        val selectedProtocol = settingsPrefs.getSelectedProtocolNow()
        val openVpnSettings = settingsPrefs.getOpenVpnSettingsNow()
        val wireGuardSettings = settingsPrefs.getWireGuardSettingsNow()
        val maceEnabled = settingsPrefs.isMaceEnabledNow()
        val selectedDnsOption = settingsPrefs.getSelectedDnsOptionNow()
        val customDns = settingsPrefs.getCustomDnsNow()
        val vpnExcludedApps = settingsPrefs.getVpnExcludedAppsNow()
        val isAllowLocalTrafficEnabled = settingsPrefs.isAllowLocalTrafficEnabledNow()
        val isAutomationEnabled = settingsPrefs.isAutomationEnabledNow()
        val isShadowsocksEnabled = settingsPrefs.isShadowsocksObfuscationEnabledNow()
        val isExternalProxyEnabled = settingsPrefs.isExternalProxyAppEnabledNow()
        val selectedShadowsocksServer = shadowsocksRegionPrefs.getSelectedShadowsocksServerNow()
        val selectedVpnServer = connectionPrefs.getSelectedVpnServerNow()

        val cipher =
            when (openVpnSettings.dataEncryption) {
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
        if (isAutomationEnabled) {
            notificationBuilder.setContentIntent(automationPendingIntent)
        } else {
            notificationBuilder.setContentIntent(configureIntent)
        }

        val protocolTarget: VPNManagerProtocolTarget
        val mtu: Int
        when (selectedProtocol) {
            VpnProtocols.WireGuard -> {
                protocolTarget = VPNManagerProtocolTarget.WIREGUARD
                mtu = wireGuardSettings.mtu
            }
            VpnProtocols.OpenVPN -> {
                protocolTarget = VPNManagerProtocolTarget.OPENVPN
                mtu = openVpnSettings.mtu
            }
        }

        return ClientConfiguration(
            sessionName = Clock.System.now().toString(),
            configureIntent = configureIntent,
            protocolTarget = protocolTarget,
            mtu = mtu,
            notificationId = NOTIFICATION_ID,
            notification = notificationBuilder.build(),
            allowedApplicationPackages = emptyList(),
            disallowedApplicationPackages = vpnExcludedApps,
            allowLocalNetworkAccess = isAllowLocalTrafficEnabled,
            serverList =
                ServerList(
                    servers =
                        getEndpoints(
                            server,
                            selectedProtocol,
                            openVpnSettings.transport,
                            openVpnSettings.dataEncryption,
                            selectedDnsOption,
                            openVpnSettings.port,
                            maceEnabled,
                            customDns,
                        ),
                ),
            openVpnClientConfiguration =
                OpenVpnClientConfiguration(
                    caCertificate = certificate,
                    username = getUsernameAndPassword().first,
                    password = getUsernameAndPassword().second,
                    socksProxy =
                        getProxyDetails(
                            isShadowsocksEnabled,
                            selectedShadowsocksServer,
                            isExternalProxyEnabled,
                            selectedVpnServer,
                        ),
                    additionalParameters = additionalOpenVpnParams,
                ),
            wireguardClientConfiguration =
                WireguardClientConfiguration(
                    token = connectionSource.getVpnToken(),
                    pinningCertificate = certificate,
                ),
        )
    }

    override suspend fun updateServerConfig(
        server: VpnServer,
        protocol: VpnProtocols,
        transport: Transport,
        dataEncryption: DataEncryption,
        selectedDnsOptions: DnsOptions,
        port: String,
        maceEnabled: Boolean,
        customDns: CustomDns,
    ): Boolean =
        connectionSource.updateConfigurationServers(
            ServerList(
                getEndpoints(
                    server,
                    protocol,
                    transport,
                    dataEncryption,
                    selectedDnsOptions,
                    port,
                    maceEnabled,
                    customDns,
                ),
            ),
        )

    private fun getServerGroup(
        protocol: VpnProtocols,
        transport: Transport,
    ): VpnServer.ServerGroup =
        when (protocol) {
            VpnProtocols.WireGuard -> VpnServer.ServerGroup.WIREGUARD
            VpnProtocols.OpenVPN -> {
                if (transport == Transport.UDP) {
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

    private fun getDnsList(
        maceEnabled: Boolean,
        selectedDnsOptions: DnsOptions,
        customDns: CustomDns,
    ): List<String> =
        if (maceEnabled) {
            listOf(MACE_DNS)
        } else {
            when (selectedDnsOptions) {
                DnsOptions.PIA -> {
                    listOf(PIA_DNS)
                }

                DnsOptions.SYSTEM -> {
                    getActiveInterfaceDnsUseCase.invoke()
                }

                DnsOptions.CUSTOM -> {
                    val result = mutableListOf<String>()
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

    private fun getEndpoints(
        server: VpnServer,
        protocol: VpnProtocols,
        transport: Transport,
        dataEncryption: DataEncryption,
        selectedDnsOptions: DnsOptions,
        port: String,
        maceEnabled: Boolean,
        customDns: CustomDns,
    ): List<ServerList.Server> {
        val details = server.endpoints[getServerGroup(protocol, transport)]
        val serverList = mutableListOf<ServerList.Server>()
        if (details.isNullOrEmpty()) {
            serverList.add(
                createServer(
                    "",
                    "",
                    8080,
                    server,
                    emptyList(),
                    transport,
                    dataEncryption,
                    selectedDnsOptions,
                ),
            )
        } else {
            for (endpoint in details) {
                if (endpoint.ip.contains(":")) {
                    val ip = endpoint.ip.substring(0, endpoint.ip.indexOf(":"))
                    val port = endpoint.ip.substring(endpoint.ip.indexOf(":") + 1).toInt()
                    serverList.add(
                        createServer(
                            ip,
                            endpoint.cn,
                            port,
                            server,
                            getDnsList(maceEnabled, selectedDnsOptions, customDns),
                            transport,
                            dataEncryption,
                            selectedDnsOptions,
                        ),
                    )
                } else {
                    val ip = endpoint.ip
                    serverList.add(
                        createServer(
                            ip,
                            endpoint.cn,
                            port.toInt(),
                            server,
                            getDnsList(maceEnabled, selectedDnsOptions, customDns),
                            transport,
                            dataEncryption,
                            selectedDnsOptions,
                        ),
                    )
                }
            }
        }
        return serverList.toList()
    }

    private fun getProxyDetails(
        shadowSocksEnabled: Boolean,
        selectedShadowsocksServer: ShadowsocksServer?,
        externalProxyEnabled: Boolean,
        selectedVpnServer: VpnServer?,
    ): OpenVpnSocksProxyDetails? {
        var proxyDetails: OpenVpnSocksProxyDetails? = null
        if (shadowSocksEnabled) {
            proxyDetails =
                selectedShadowsocksServer?.let {
                    OpenVpnSocksProxyDetails(
                        clientProxyAddress = OBFUSCATOR_PROXY_HOST,
                        clientProxyPort = OBFUSCATOR_PROXY_PORT,
                        serverProxyAddress = it.host,
                    )
                }
        }
        if (externalProxyEnabled) {
            proxyDetails =
                selectedVpnServer?.let {
                    OpenVpnSocksProxyDetails(
                        clientProxyAddress = OBFUSCATOR_PROXY_HOST,
                        clientProxyPort = connectionPrefs.proxyPort.value.toInt(),
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
        transport: Transport,
        dataEncryption: DataEncryption,
        selectedDnsOptions: DnsOptions,
    ): ServerList.Server =
        ServerList.Server(
            ip = ip,
            port = port,
            commonOrDistinguishedName = cn,
            transport =
                when (transport) {
                    Transport.UDP -> TransportProtocol.UDP
                    Transport.TCP -> TransportProtocol.TCP
                },
            ciphers =
                when (dataEncryption) {
                    DataEncryption.AES_128_GCM -> listOf(ProtocolCipher.AES_128_GCM)
                    DataEncryption.AES_256_GCM -> listOf(ProtocolCipher.AES_256_GCM)
                    DataEncryption.CHA_CHA_20 -> listOf(ProtocolCipher.CHA_CHA_20)
                },
            latency = server.latency?.toLong(),
            dnsInformation =
                DnsInformation(
                    dnsList = dnsList,
                    systemDnsResolverEnabled = selectedDnsOptions == DnsOptions.SYSTEM,
                ),
        )
}