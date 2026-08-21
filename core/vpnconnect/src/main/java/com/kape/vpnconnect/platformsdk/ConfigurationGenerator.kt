package com.kape.vpnconnect.platformsdk

import android.content.Context
import com.kape.data.vpnserver.VpnServer
import com.kape.localprefs.prefs.ConnectionPrefs
import com.kape.localprefs.prefs.SettingsPrefs
import com.kape.platformsdk.vpn.openvpn.OpenVpnConfiguration
import com.kape.platformsdk.vpn.openvpn.OpenVpnTransport
import com.kape.platformsdk.vpn.service.interfaces.VpnConfiguration
import com.kape.platformsdk.vpn.service.interfaces.VpnConfigurationGenerator
import com.kape.platformsdk.vpn.service.models.IpAddress
import com.kape.platformsdk.vpn.wireguard.WireGuardEndpointConfiguration
import com.kape.platformsdk.vpn.wireguard.WireGuardObfuscation
import com.kape.platformsdk.vpn.wireguard.WireGuardVpnConfiguration
import com.kape.settings.data.CustomDns
import com.kape.settings.data.DataEncryption
import com.kape.settings.data.DnsOptions
import com.kape.settings.data.OpenVpnSettings
import com.kape.settings.data.Transport
import com.kape.settings.data.VpnProtocols
import com.kape.settings.data.WireGuardSettings
import com.kape.vpnconnect.domain.ConnectionDataSource
import com.kape.vpnconnect.domain.GetActiveInterfaceDnsUseCase

// PIA's WireGuard addKey control-plane port — fixed, independent of the user's configurable WG data port.
private const val WG_AUTH_PORT = 1337

// PIA's fixed internal resolvers, reachable through any server's tunnel — the tunnel gateway
// itself does not run a DNS resolver, so these must be pushed explicitly rather than relying
// on the SDK's gateway-address fallback.
private const val PIA_DNS = "10.0.0.243"
private const val MACE_DNS = "10.0.0.241"

class ConfigurationGenerator(
    private val caCertificate: String,
    private val connectionSource: ConnectionDataSource,
    private val settingsPrefs: SettingsPrefs,
    private val connectionPrefs: ConnectionPrefs,
    private val getActiveInterfaceDnsUseCase: GetActiveInterfaceDnsUseCase,
    private val context: Context,
) : VpnConfigurationGenerator {
    override suspend fun generateConfigurations(): List<VpnConfiguration> =
        when (settingsPrefs.getSelectedProtocolNow()) {
            VpnProtocols.OpenVPN ->
                generateOpenVpnConfigurations(
                    settingsPrefs.getOpenVpnSettingsNow(),
                    connectionPrefs.getSelectedVpnServerNow(),
                    getDnsServers(),
                )

            VpnProtocols.WireGuard ->
                generateWireGuardVpnConfigurations(
                    settingsPrefs.getWireGuardSettingsNow(),
                    connectionPrefs.getSelectedVpnServerNow(),
                )

            VpnProtocols.Automatic ->
                generateAutomaticConfigurations(
                    connectionPrefs.getSelectedVpnServerNow(),
                    getDnsServers(),
                )
        }

    private suspend fun getDnsServers(): List<String> =
        if (settingsPrefs.isMaceEnabledNow()) {
            listOf(MACE_DNS)
        } else {
            when (settingsPrefs.getSelectedDnsOptionNow()) {
                DnsOptions.PIA -> listOf(PIA_DNS)
                DnsOptions.SYSTEM -> getActiveInterfaceDnsUseCase()
                DnsOptions.CUSTOM -> {
                    val customDns: CustomDns = settingsPrefs.getCustomDnsNow()
                    listOfNotNull(
                        customDns.primaryDns.takeIf { it.isNotEmpty() },
                        customDns.secondaryDns.takeIf { it.isNotEmpty() },
                    )
                }
            }
        }

    fun generateOpenVpnConfigurations(
        openVpnSettings: OpenVpnSettings,
        server: VpnServer?,
        dnsServers: List<String> = emptyList(),
    ): List<OpenVpnConfiguration> {
        val result = mutableListOf<OpenVpnConfiguration>()
        val transport =
            when (openVpnSettings.transport) {
                Transport.UDP -> OpenVpnTransport.UDP
                Transport.TCP -> OpenVpnTransport.TCP
                Transport.AUTO -> OpenVpnTransport.UDP
            }
        val port = openVpnSettings.port.toInt()
        val mtu = openVpnSettings.mtu
        val username = getUsernameAndPassword().first
        val password = getUsernameAndPassword().second
        val serverGroup =
            when (transport) {
                OpenVpnTransport.UDP -> VpnServer.ServerGroup.OPENVPN_UDP
                OpenVpnTransport.TCP -> VpnServer.ServerGroup.OPENVPN_TCP
                null -> null
            }

        fun params(
            ip: String,
            cn: String,
            port: Int,
        ): String {
            val cipher =
                when (openVpnSettings.dataEncryption) {
                    DataEncryption.AES_128_GCM -> "AES-128-GCM"
                    DataEncryption.AES_256_GCM -> "AES-256-GCM"
                    DataEncryption.CHA_CHA_20 -> "CHACHA20-POLY1305"
                    DataEncryption.AUTO -> "" // never used
                }
            val commandLineParams =
                mutableListOf(
                    "status-version 3",
                    "machine-readable-output",
                    "management-query-passwords",
                    "management-forget-disconnect",
                    "management-hold",
                    "management ${context.applicationInfo.dataDir}/management unix",
                    "tmp-dir ${context.applicationInfo.dataDir}/tmp",
                    "remote $ip $port",
                    "dev tun",
                    "auth-user-pass",
                    "client",
                    "proto ${transport?.name?.lowercase()}",
                    "connect-retry 2 300",
                    "allow-recursive-routing",
                    "resolv-retry infinite",
                    "persist-key",
                    "persist-tun",
                    "nobind",
                    "data-ciphers $cipher",
                    "auth SHA256",
                    "auth-nocache",
                    "explicit-exit-notify 2",
                    "script-security 2",
                    "remote-cert-tls server",
                    "verb 3",
                    "mute-replay-warnings",
                    "block-ipv6",
                    "mssfix $mtu",
                )
            if (server?.isDedicatedIp == true) {
                commandLineParams.add("ncp-disable")
                commandLineParams.add("pia-signal-settings")
            }
            val builder = StringBuilder()
            commandLineParams.forEach {
                builder.append(it)
                builder.append("\n")
            }
            return builder.toString()
        }
        server?.endpoints[serverGroup]?.forEach { details ->
            result.add(
                OpenVpnConfiguration(
                    host = details.ip,
                    port = port,
                    transport = transport,
                    ovpnConfiguration = params(details.ip, details.cn, port),
                    xorValue = null,
                    mtu = mtu,
                    certDn = details.cn,
                    username = username,
                    password = password,
                    caCertificate = caCertificate,
                    clientCertificate = "",
                    clientKey = "",
                    tlsAuthKey = "",
                    dnsServers = dnsServers,
                ),
            )
        }

        return result
    }

    fun generateWireGuardVpnConfigurations(
        wireGuardSettings: WireGuardSettings,
        server: VpnServer?,
    ): List<WireGuardVpnConfiguration> {
        val result = mutableListOf<WireGuardVpnConfiguration>()
        server?.endpoints[VpnServer.ServerGroup.WIREGUARD]?.forEach {
            val wgIp = it.ip.substring(0, it.ip.indexOf(":"))
            result.add(
                WireGuardVpnConfiguration(
                    endpointConfiguration =
                        WireGuardEndpointConfiguration(
                            ip = IpAddress.V4(wgIp),
                            port = wireGuardSettings.port.toInt(),
                            authIp = IpAddress.V4(wgIp),
                            authPort = WG_AUTH_PORT,
                            certDn = it.cn,
                            obfuscation = WireGuardObfuscation.None,
                        ),
                    host = wgIp,
                    port = wireGuardSettings.port.toInt(),
                    obfuscation = WireGuardObfuscation.None,
                    mtu = wireGuardSettings.mtu,
                ),
            )
        }
        return result
    }

    private fun generateAutomaticConfigurations(
        server: VpnServer?,
        dnsServers: List<String> = emptyList(),
    ): List<VpnConfiguration> {
        val result =
            mutableListOf<VpnConfiguration>().apply {
                addAll(generateWireGuardVpnConfigurations(automaticWireGuardSettings(), server))
                addAll(
                    generateOpenVpnConfigurations(
                        automaticOpenVpnUdpSettings(),
                        server,
                        dnsServers,
                    ),
                )
                addAll(
                    generateOpenVpnConfigurations(
                        automaticOpenVpnTcpSettings(),
                        server,
                        dnsServers,
                    ),
                )
            }

        return result
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

    private fun automaticWireGuardSettings() = WireGuardSettings()

    private fun automaticOpenVpnUdpSettings() = OpenVpnSettings()

    private fun automaticOpenVpnTcpSettings() = OpenVpnSettings(transport = Transport.TCP, port = "80")
}