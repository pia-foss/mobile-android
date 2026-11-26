package com.kape.settings.domain.usecase

import com.kape.data.vpnserver.VpnServer
import com.kape.localprefs.api.CsiStore
import com.kape.localprefs.api.SettingsStore
import com.kape.regions.vpn.data.openVpnApiKey
import com.kape.regions.vpn.domain.usecase.GetVpnServerInfoUseCase
import com.kape.settings.data.Transport
import com.kape.settings.data.VpnProtocols
import org.koin.core.annotation.Factory

private const val DEFAULT_UDP_PORT = 8080
private const val DEFAULT_TCP_PORT = 80

// Shadowsocks obfuscation only works over OpenVPN with TCP, and needs local network traffic
// allowed, so turning any of those off turns it off too.

@Factory
class SelectProtocolUseCase(
    private val prefs: SettingsStore,
) {
    suspend operator fun invoke(protocol: VpnProtocols) {
        prefs.setSelectedProtocol(protocol)
        if (protocol != VpnProtocols.OpenVPN) prefs.setShadowsocksObfuscationEnabled(false)
    }
}

@Factory
class SetTransportUseCase(
    private val prefs: SettingsStore,
    private val getServerInfo: GetVpnServerInfoUseCase,
) {
    suspend operator fun invoke(transport: Transport): Boolean {
        val current = prefs.getOpenVpnSettingsNow()
        val info = getServerInfo()
        val port =
            if (transport == Transport.UDP) {
                info.udpPorts?.firstOrNull() ?: DEFAULT_UDP_PORT
            } else {
                info.tcpPorts?.firstOrNull() ?: DEFAULT_TCP_PORT
            }
        prefs.setOpenVpnSettings(current.copy(transport = transport, port = port.toString()))
        if (transport == Transport.UDP) prefs.setShadowsocksObfuscationEnabled(false)
        return current.transport != transport
    }
}

@Factory
class SetAllowLocalNetworkUseCase(
    private val prefs: SettingsStore,
) {
    suspend operator fun invoke(enabled: Boolean) {
        prefs.setAllowLocalTrafficEnabled(enabled)
        if (!enabled) prefs.setShadowsocksObfuscationEnabled(false)
    }
}

@Factory
class SetDebugLoggingUseCase(
    private val prefs: SettingsStore,
    private val csiPrefs: CsiStore,
) {
    suspend operator fun invoke(enabled: Boolean) {
        prefs.setDebugLoggingEnabled(enabled)
        if (!enabled) csiPrefs.clearProtocolDebugLogs()
    }
}

// Reads the stored list: prefs' StateFlow is empty while uncollected, so writing from it would wipe the list.
@Factory
class EditExcludedAppsUseCase(
    private val prefs: SettingsStore,
) {
    suspend fun add(packageName: String) {
        val current = prefs.getVpnExcludedAppsNow()
        if (packageName !in current) prefs.setVpnExcludedApps(current + packageName)
    }

    suspend fun remove(packageName: String) = prefs.setVpnExcludedApps(prefs.getVpnExcludedAppsNow() - packageName)
}

@Factory
class SetExternalProxyAppUseCase(
    private val prefs: SettingsStore,
    private val excludedApps: EditExcludedAppsUseCase,
) {
    suspend operator fun invoke(packageName: String) {
        val previous = prefs.getExternalProxyAppPackageNameNow()
        if (previous.isNotEmpty() && previous != packageName) excludedApps.remove(previous)
        prefs.setExternalProxyAppPackageName(packageName)
        if (packageName.isNotEmpty()) excludedApps.add(packageName)
        prefs.setExternalProxyAppEnabled(packageName.isNotEmpty())
    }
}

internal fun dedicatedIpOpenVpnPorts(
    dedicatedIpPorts: List<Map<String, List<Int>>>,
    transport: Transport,
): List<Int> {
    val key =
        when (transport) {
            Transport.UDP -> VpnServer.ServerGroup.OPENVPN_UDP
            Transport.TCP -> VpnServer.ServerGroup.OPENVPN_TCP
            Transport.AUTO -> null
        }?.openVpnApiKey() ?: return emptyList()
    return dedicatedIpPorts.flatMap { it[key].orEmpty() }.distinct()
}