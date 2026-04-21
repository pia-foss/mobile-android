package com.kape.portforwarding.domain

import com.kape.connection.model.PortBindInformation

interface PortForwardingApi {

    suspend fun getPayloadAndSignature(vpnToken: String, gateway: String): PortBindInformation?

    suspend fun bindPort(token: String, payload: String, signature: String, endpoint: String): Boolean

    fun setKnownEndpointCommonName(knownEndpointCommonName: List<Pair<String, String>>)
}