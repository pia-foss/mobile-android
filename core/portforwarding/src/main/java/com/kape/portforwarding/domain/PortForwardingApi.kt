package com.kape.portforwarding.domain

import com.kape.portforwarding.data.model.PayloadAndSignature
import kotlinx.coroutines.flow.Flow

interface PortForwardingApi {

    fun getPayloadAndSignature(vpnToken: String, gateway: String): Flow<PayloadAndSignature?>

    fun bindPort(token: String, payload: String, signature: String, endpoint: String): Flow<Boolean>

    fun setKnownEndpointCommonName(knownEndpointCommonName: List<Pair<String, String>>)
}