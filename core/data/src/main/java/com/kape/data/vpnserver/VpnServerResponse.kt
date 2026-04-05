package com.kape.data.vpnserver

data class VpnServerResponse(
    private val servers: Map<String, VpnServer>? = null,
    private val info: VpnServerInfo? = null,
    private val body: String? = null,
) {

    fun isValid(): Boolean {
        return !servers.isNullOrEmpty() && info != null && info.isValid()
    }
}