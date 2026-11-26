package com.kape.portforwarding.data

import com.kape.data.portforwarding.PortForwardingStatus
import com.kape.data.vpnserver.VpnServer
import com.kape.localprefs.prefs.ConnectionPrefs
import com.kape.localprefs.prefs.SettingsPrefs
import com.kape.network.pinning.EndpointIdentity
import com.kape.portforwarding.domain.PortForwardingApi
import com.kape.settings.data.Transport
import com.kape.settings.data.VpnProtocols
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.koin.core.annotation.Singleton
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

private const val MIN_EXPIRATION_DAYS = 7

@Singleton
class PortForwardingManager(
    private val api: PortForwardingApi,
    private val connectionPrefs: ConnectionPrefs,
    private val settingsPrefs: SettingsPrefs,
) {
    private val _portForwardingStatus = MutableStateFlow<PortForwardingStatus>(PortForwardingStatus.NoPortForwarding)
    val portForwardingStatus: StateFlow<PortForwardingStatus> = _portForwardingStatus.asStateFlow()
    private val _port = MutableStateFlow("")
    val port: StateFlow<String> = _port.asStateFlow()

    suspend fun bindPort(vpnToken: String) {
        _portForwardingStatus.value = PortForwardingStatus.Requesting
        val gateway = connectionPrefs.getGatewayNow()
        val server = connectionPrefs.getSelectedVpnServerNow()
        if (gateway.isEmpty() || server == null || vpnToken.isEmpty()) {
            _portForwardingStatus.value = PortForwardingStatus.Error
            return
        }
        val identities = gatewayIdentities(gateway, server)

        val existing = connectionPrefs.getPortBindingInfoNow()
        if (existing != null && daysLeft(existing.decodedPayload.expirationDate) > MIN_EXPIRATION_DAYS) {
            val bound = api.bindPort(existing.decodedPayload.token, existing.payload, existing.signature, identities)
            onBound(bound, existing.decodedPayload.port)
            return
        }

        val assignment = api.getPayloadAndSignature(vpnToken, identities)
        connectionPrefs.setPortBindingInformation(assignment)
        if (assignment == null) {
            _portForwardingStatus.value = PortForwardingStatus.Error
            return
        }
        val bound = api.bindPort(assignment.decodedPayload.token, assignment.payload, assignment.signature, identities)
        onBound(bound, assignment.decodedPayload.port)
    }

    fun clearBindPort() {
        _portForwardingStatus.value = PortForwardingStatus.NoPortForwarding
        _port.value = ""
    }

    private fun onBound(
        bound: Boolean,
        port: Int,
    ) {
        _portForwardingStatus.value = if (bound) PortForwardingStatus.Success else PortForwardingStatus.Error
        if (bound) _port.value = port.toString()
    }

    private suspend fun gatewayIdentities(
        gateway: String,
        server: VpnServer,
    ): List<EndpointIdentity> {
        val endpoints = serverGroup()?.let { server.endpoints[it].orEmpty() } ?: server.endpoints.values.flatten()
        return endpoints.map { EndpointIdentity(endpoint = gateway, commonName = it.cn) }
    }

    private suspend fun serverGroup(): VpnServer.ServerGroup? =
        when (settingsPrefs.getSelectedProtocolNow()) {
            VpnProtocols.WireGuard -> VpnServer.ServerGroup.WIREGUARD
            VpnProtocols.OpenVPN ->
                if (settingsPrefs.getOpenVpnSettingsNow().transport == Transport.UDP) {
                    VpnServer.ServerGroup.OPENVPN_UDP
                } else {
                    VpnServer.ServerGroup.OPENVPN_TCP
                }
            VpnProtocols.Automatic -> null
        }

    private fun daysLeft(expirationDate: String): Long {
        val expires = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).parse(expirationDate) ?: return 0
        return TimeUnit.DAYS.convert(expires.time - Date().time, TimeUnit.MILLISECONDS)
    }
}