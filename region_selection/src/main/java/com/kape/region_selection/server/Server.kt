package com.kape.region_selection.server

data class Server(
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
    val isAllowsPF: Boolean,
    val dipToken: String?,
    val dedicatedIp: String?,
) {
    data class ServerEndpointDetails(
        val ip: String,
        val cn: String,
        val usesVanillaOpenVPN: Boolean,
    )

    enum class ServerGroup {
        OPENVPN_TCP {
            override fun toString(): String {
                return "ovpntcp"
            }
        },
        OPENVPN_UDP {
            override fun toString(): String {
                return "ovpnudp"
            }
        },
        WIREGUARD {
            override fun toString(): String {
                return "wg"
            }
        },
        META {
            override fun toString(): String {
                return "meta"
            }
        }
    }

    fun isDedicatedIp() = dedicatedIp != null && dedicatedIp.isNotEmpty()
}