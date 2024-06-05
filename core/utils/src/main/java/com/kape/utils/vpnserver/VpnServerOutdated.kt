package com.kape.utils.vpnserver

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VpnServerOutdated(
    val name: String,
    val iso: String,
    val dns: String,
    var latency: String?,
    val endpoints: Map<VpnServer.ServerGroup, List<VpnServer.ServerEndpointDetails>>,
    val key: String,
    val latitude: String?,
    val longitude: String?,
    val isGeo: Boolean,
    val isOffline: Boolean,
    @SerialName("isAllowsPF")
    val allowsPortForwarding: Boolean,
    val dipToken: String?,
    val dedicatedIp: String?,
    val isDedicatedIp: Boolean = !dedicatedIp.isNullOrEmpty(),
) {
    fun toVpnServer(): VpnServer = VpnServer(
        name,
        iso,
        dns,
        latency,
        endpoints,
        key,
        latitude,
        longitude,
        isGeo,
        isOffline,
        allowsPortForwarding,
        false,
        dipToken,
        dedicatedIp,
        isDedicatedIp,
    )
}