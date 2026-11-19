package com.kape.regions.vpn.domain

import com.kape.data.vpnserver.VpnServer
import com.kape.data.vpnserver.VpnServerInfo
import kotlinx.coroutines.flow.Flow

const val VPN_PING_TIMEOUT_MS = 1500L

interface VpnRegionRepository {
    fun observeServers(): Flow<List<VpnServer>>

    fun observeIsDefaultList(): Flow<Boolean>

    suspend fun refreshServers(locale: String)

    suspend fun refreshLatencies()

    fun clearLatencies()

    fun hasLatencies(): Boolean

    suspend fun getServerInfo(): VpnServerInfo
}