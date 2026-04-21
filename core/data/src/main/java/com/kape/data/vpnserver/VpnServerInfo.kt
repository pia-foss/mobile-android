package com.kape.data.vpnserver

data class VpnServerInfo(
    var autoRegions: List<String>? = null,
    var udpPorts: List<Int>? = null,
    var tcpPorts: List<Int>? = null,
) {
    fun isValid(): Boolean = !autoRegions.isNullOrEmpty() && !udpPorts.isNullOrEmpty() && !tcpPorts.isNullOrEmpty()

    override fun toString() = "PIAServerInfo { autoRegions: $autoRegions, udpPorts: $udpPorts, tcpPorts: $tcpPorts }"
}