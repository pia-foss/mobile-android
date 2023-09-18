package com.kape.portforwarding.domain

import androidx.compose.runtime.mutableStateOf
import com.kape.connection.ConnectionPrefs
import com.kape.connection.model.PortBindInformation
import com.kape.portforwarding.data.model.PortForwardingStatus
import com.kape.vpnconnect.domain.ConnectionUseCase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.concurrent.TimeUnit

private const val MIN_EXPIRATION_DAYS = 7

class PortForwardingUseCase(
    private val api: PortForwardingApi,
    private val connectionPrefs: ConnectionPrefs,
    private val connectionUseCase: ConnectionUseCase,
) {

    val portForwardingStatus =
        mutableStateOf<PortForwardingStatus>(PortForwardingStatus.NoPortForwarding)

    suspend fun bindPort() {
        val gateway = connectionPrefs.getGateway()
        if (gateway.isEmpty()) {
            portForwardingStatus.value = PortForwardingStatus.Error
        }

        // Set the gateway's CN for the selected protocol before the binding request
        val server = connectionPrefs.getSelectedServer()
        server?.let {
            it.endpoints[connectionUseCase.getServerGroup()]?.let { serverEndpointDetails ->
                val tunnelCommonName = mutableListOf<Pair<String, String>>()
                for ((_, commonName) in serverEndpointDetails) {
                    tunnelCommonName.add(Pair(gateway, commonName))
                }
                api.setKnownEndpointCommonName(tunnelCommonName)
            }
        }

        // If there is active data persisted. Send the bind port reminder request to keep the NAT
        // on the server rather than requesting a new port
        connectionPrefs.getPortBindingInfo()?.let {
            if (tokenExpirationDateDaysLeft(it.decodedPayload.expirationDate) > MIN_EXPIRATION_DAYS) {
                portForwardingStatus.value = PortForwardingStatus.Requesting
                api.bindPort(it.decodedPayload.token, it.payload, it.signature, gateway).collect {
                    if (it) {
                        portForwardingStatus.value = PortForwardingStatus.Success
                    } else {
                        portForwardingStatus.value = PortForwardingStatus.Error
                    }
                }
            }
        }

        val vpnToken = connectionUseCase.getVpnToken()
        if (vpnToken.isEmpty()) {
            portForwardingStatus.value = PortForwardingStatus.Error
        }

        portForwardingStatus.value = PortForwardingStatus.Requesting
        api.getPayloadAndSignature(vpnToken, gateway).collect {
            if (it != null) {
                api.bindPort(it.payload.token, it.payload.toString(), it.signature, gateway)
                    .collect { successful ->
                        if (successful) {
                            val portBindInformation =
                                PortBindInformation(it.payload.toString(), it.signature, it.payload)
                            connectionPrefs.setPortBindingInformation(portBindInformation)
                            portForwardingStatus.value = PortForwardingStatus.Success
                        } else {
                            portForwardingStatus.value = PortForwardingStatus.Error
                        }
                    }
            } else {
                portForwardingStatus.value = PortForwardingStatus.Error
            }
        }
    }

    private fun tokenExpirationDateDaysLeft(tokenExpirationDate: String): Long {
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        val expirationDate = format.parse(tokenExpirationDate)
        return TimeUnit.DAYS.convert(expirationDate.time - Date().time, TimeUnit.MILLISECONDS)
    }
}