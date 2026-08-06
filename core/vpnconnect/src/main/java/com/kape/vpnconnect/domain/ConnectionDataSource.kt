package com.kape.vpnconnect.domain

interface ConnectionDataSource {
    fun getVpnToken(): String

    fun startPortForwarding()

    fun stopPortForwarding()

    suspend fun getDebugLogs(): List<String>
}