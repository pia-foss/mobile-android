package com.kape.portforwarding.domain

import com.kape.connection.model.PortBindInformation
import com.kape.network.pinning.EndpointIdentity

interface PortForwardingApi {
    suspend fun getPayloadAndSignature(
        vpnToken: String,
        gatewayIdentities: List<EndpointIdentity>,
    ): PortBindInformation?

    suspend fun bindPort(
        token: String,
        payload: String,
        signature: String,
        gatewayIdentities: List<EndpointIdentity>,
    ): Boolean
}