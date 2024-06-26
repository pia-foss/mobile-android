package com.kape.utils.vpnserver

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
    )

    @Serializable
    enum class ServerGroup {
        OPENVPN_TCP {
            override fun toString(): String {
                return "openvpn_tcp"
            }
        },
        OPENVPN_UDP {
            override fun toString(): String {
                return "openvpn_udp"
            }
        },
        WIREGUARD {
            override fun toString(): String {
                return "wireguard"
            }
        },
        META {
            override fun toString(): String {
                return "meta"
            }
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

        if (key != other.key || isDedicatedIp != other.isDedicatedIp || iso != other.iso) {
            return false
        }

        return true
    }
}