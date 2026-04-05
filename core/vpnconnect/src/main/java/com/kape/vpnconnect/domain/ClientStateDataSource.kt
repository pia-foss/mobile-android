package com.kape.vpnconnect.domain

interface ClientStateDataSource {
    suspend fun getPublicIp(): String
    suspend fun getVpnIp(): String
}