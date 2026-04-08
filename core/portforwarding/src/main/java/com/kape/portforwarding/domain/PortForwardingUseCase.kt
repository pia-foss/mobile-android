package com.kape.portforwarding.domain

import com.kape.data.portforwarding.PortForwardingStatus
import com.kape.data.vpnserver.VpnServer
import com.kape.localprefs.prefs.ConnectionPrefs
import com.kape.localprefs.prefs.SettingsPrefs
import com.kape.settings.data.Transport
import com.kape.settings.data.VpnProtocols
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
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
        val context = currentCoroutineContext()
        context.ensureActive()
        portForwardingStatus.value = PortForwardingStatus.Requesting
        val gateway = connectionPrefs.getGateway()
        if (gateway.isEmpty()) {
            portForwardingStatus.value = PortForwardingStatus.Error
            return
        }

        // Set known endpoint CN
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

        context.ensureActive()
        if (vpnToken.isEmpty()) {
            portForwardingStatus.value = PortForwardingStatus.Error
            return
        }

        connectionPrefs.getPortBindingInfo()?.let { portBindingInfo ->
            context.ensureActive()

            if (tokenExpirationDateDaysLeft(portBindingInfo.decodedPayload.expirationDate) > MIN_EXPIRATION_DAYS) {
                portForwardingStatus.value = PortForwardingStatus.Requesting
                val successful = api.bindPort(
                    portBindingInfo.decodedPayload.token,
                    portBindingInfo.payload,
                    portBindingInfo.signature,
                    gateway,
                )
                context.ensureActive()
                if (successful) {
                    portForwardingStatus.value = PortForwardingStatus.Success
                    port.value = portBindingInfo.decodedPayload.port.toString()
                } else {
                    portForwardingStatus.value = PortForwardingStatus.Error
                }
            }
        } ?: run {
            context.ensureActive()
            portForwardingStatus.value = PortForwardingStatus.Requesting

            val it = api.getPayloadAndSignature(vpnToken, gateway)
            context.ensureActive()
            connectionPrefs.setPortBindingInformation(it)

            if (it != null) {
                val successful = api.bindPort(it.decodedPayload.token, it.payload, it.signature, gateway)
                context.ensureActive()
                if (successful) {
                    portForwardingStatus.value = PortForwardingStatus.Success
                    port.value = it.decodedPayload.port.toString()
                } else {
                    portForwardingStatus.value = PortForwardingStatus.Error
                }
            } else {
                portForwardingStatus.value = PortForwardingStatus.Error
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