package com.kape.portforwarding.domain

import com.kape.localprefs.prefs.ConnectionPrefs
import com.kape.localprefs.prefs.SettingsPrefs
import com.kape.portforwarding.data.model.PortForwardingStatus
import com.kape.settings.data.Transport
import com.kape.settings.data.VpnProtocols
import com.kape.utils.vpnserver.VpnServer
import kotlinx.coroutines.flow.MutableStateFlow
import org.koin.core.annotation.Singleton
import java.text.SimpleDateFormat
import java.util.Date
import java.util.concurrent.TimeUnit

private const val MIN_EXPIRATION_DAYS = 7

@Singleton
class PortForwardingUseCase(
    private val api: PortForwardingApi,
    private val connectionPrefs: ConnectionPrefs,
    private val settingsPrefs: SettingsPrefs,
) {
    val portForwardingStatus =
        MutableStateFlow<PortForwardingStatus>(PortForwardingStatus.NoPortForwarding)
    val port = MutableStateFlow<String>("")

    suspend fun bindPort(vpnToken: String) {
        portForwardingStatus.value = PortForwardingStatus.Requesting
        val gateway = connectionPrefs.getGateway()
        if (gateway.isEmpty()) {
            portForwardingStatus.value = PortForwardingStatus.Error
        }

        // Set the gateway's CN for the selected protocol before the binding request
        val server = connectionPrefs.getSelectedVpnServer()
        server?.let {
            it.endpoints[getServerGroup()]?.let { serverEndpointDetails ->
                val tunnelCommonName = mutableListOf<Pair<String, String>>()
                for ((_, commonName) in serverEndpointDetails) {
                    tunnelCommonName.add(Pair(gateway, commonName))
                }
                api.setKnownEndpointCommonName(tunnelCommonName)
            }
        }

        if (vpnToken.isEmpty()) {
            portForwardingStatus.value = PortForwardingStatus.Error
        }

        // If there is active data persisted. Send the bind port reminder request to keep the NAT
        // on the server rather than requesting a new port
        connectionPrefs.getPortBindingInfo()?.let { portBindingInfo ->
            if (tokenExpirationDateDaysLeft(portBindingInfo.decodedPayload.expirationDate) > MIN_EXPIRATION_DAYS) {
                portForwardingStatus.value = PortForwardingStatus.Requesting
                api.bindPort(
                    portBindingInfo.decodedPayload.token,
                    portBindingInfo.payload,
                    portBindingInfo.signature,
                    gateway,
                ).collect {
                    if (it) {
                        portForwardingStatus.value = PortForwardingStatus.Success
                        port.value = portBindingInfo.decodedPayload.port.toString()
                    } else {
                        portForwardingStatus.value = PortForwardingStatus.Error
                    }
                }
            }
        } ?: run {
            portForwardingStatus.value = PortForwardingStatus.Requesting
            api.getPayloadAndSignature(vpnToken, gateway).collect {
                connectionPrefs.setPortBindingInformation(it)
                if (it != null) {
                    api.bindPort(it.decodedPayload.token, it.payload, it.signature, gateway)
                        .collect { successful ->
                            if (successful) {
                                portForwardingStatus.value = PortForwardingStatus.Success
                                port.value = it.decodedPayload.port.toString()
                            } else {
                                portForwardingStatus.value = PortForwardingStatus.Error
                            }
                        }
                } else {
                    portForwardingStatus.value = PortForwardingStatus.Error
                }
            }
        }
    }

    fun clearBindPort() {
        portForwardingStatus.value = PortForwardingStatus.NoPortForwarding
        port.value = ""
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

    private fun tokenExpirationDateDaysLeft(tokenExpirationDate: String): Long {
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        val expirationDate = format.parse(tokenExpirationDate)
        return TimeUnit.DAYS.convert(expirationDate.time - Date().time, TimeUnit.MILLISECONDS)
    }
}