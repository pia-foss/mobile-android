package com.kape.localprefs.api

import com.kape.connection.model.QuickConnectServer
import com.kape.data.vpnserver.VpnServer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface ConnectionStore {
    suspend fun getLastSnoozeEndTimeNow(): Long

    suspend fun getSelectedVpnServerNow(): VpnServer?

    fun observeSelectedVpnServer(): Flow<VpnServer?>

    val proxyPort: StateFlow<String>

    val quickConnectServers: StateFlow<List<QuickConnectServer>>

    suspend fun setDisconnectedByUser(byUser: Boolean)

    suspend fun setProxyPort(port: String?)
}