package com.kape.contracts

interface VpnConnectionControl {
    fun isConnected(): Boolean

    fun isConnectionInProgress(): Boolean

    suspend fun disconnect()
}