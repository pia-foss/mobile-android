package com.kape.data.vpnserver

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VpnServer(
    val name: String,
    val iso: String,
    val dns: String,
    var latency: String?,
    val endpoints: Map<ServerGroup, List<ServerEndpointDetails>>,
    val key: String,
    val latitude: String?,
    val longitude: String?,
    val isGeo: Boolean,
    val isOffline: Boolean,
    @SerialName("isAllowsPF")
    val allowsPortForwarding: Boolean,
    val autoRegion: Boolean,
    val dipToken: String?,
    val dedicatedIp: String?,
    val isDedicatedIp: Boolean = !dedicatedIp.isNullOrEmpty(),
) {
    @Serializable
    data class ServerEndpointDetails(
        val ip: String,
        val cn: String,
        val port: Int? = null,
    )

    @Serializable
    enum class ServerGroup {
        OPENVPN_TCP {
            override fun toString(): String = "openvpn_tcp"
        },
        OPENVPN_UDP {
            override fun toString(): String = "openvpn_udp"
        },
        WIREGUARD {
            override fun toString(): String = "wireguard"
        },
        META {
            override fun toString(): String = "meta"
        },
        AMNEZIA {
            override fun toString(): String = "awg"
        },
    }

    override fun equals(other: Any?): Boolean {
        if (other == null) {
            return false
        }

        if (other !is VpnServer) {
            return false
        }

        if (this === other) {
            return true
        }

        // A region and each dedicated IP in it are different servers; latency and endpoints change between refreshes.
        if (key != other.key || iso != other.iso || dedicatedIpOrNull != other.dedicatedIpOrNull) {
            return false
        }

        return true
    }

    // Must hash only what equals compares, or sets and distinct() treat equal servers as different.
    override fun hashCode(): Int {
        var result = key.hashCode()
        result = 31 * result + iso.hashCode()
        result = 31 * result + dedicatedIpOrNull.hashCode()
        return result
    }

    private val dedicatedIpOrNull: String?
        get() = dedicatedIp?.takeIf { isDedicatedIp }
}