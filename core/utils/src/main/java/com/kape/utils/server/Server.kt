package com.kape.utils.server

data class Server(
    val name: String,
    val iso: String,
    var latency: String?,
    val endpoints: Map<ServerGroup, List<ServerEndpointDetails>>,
    val key: String,
    val latitude: String?,
    val longitude: String?,
    val isGeo: Boolean,
    val isOffline: Boolean,
    val isAllowsPF: Boolean,
    val dipToken: String?,
    val dedicatedIp: String?,
    val isDedicatedIp: Boolean = !dedicatedIp.isNullOrEmpty()
) {
    data class ServerEndpointDetails(
        val ip: String,
        val cn: String
    )

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
        }
    }
}