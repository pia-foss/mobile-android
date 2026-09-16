package com.kape.vpnconnect.domain

interface ClientStateDataSource {
    suspend fun getPublicIp(): String

    suspend fun getVpnIp(): String

    /** Single-shot check of whether the server currently sees this client as connected via VPN. */
    suspend fun isVpnTunnelReachable(): Boolean
}