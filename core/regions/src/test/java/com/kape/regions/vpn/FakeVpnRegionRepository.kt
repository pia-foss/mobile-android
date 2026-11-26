package com.kape.regions.vpn

import com.kape.data.vpnserver.VpnServer
import com.kape.data.vpnserver.VpnServerInfo
import com.kape.regions.vpn.domain.VpnRegionRepository
import kotlinx.coroutines.flow.MutableStateFlow

internal class FakeVpnRegionRepository(
    servers: List<VpnServer> = emptyList(),
) : VpnRegionRepository {
    val servers = MutableStateFlow(servers)
    val isDefaultList = MutableStateFlow(true)
    var serverInfo = VpnServerInfo()

    override fun observeServers() = servers

    override fun observeIsDefaultList() = isDefaultList

    override suspend fun refreshServers(locale: String) = Unit

    override suspend fun refreshLatencies() = Unit

    override fun clearLatencies() = Unit

    override fun hasLatencies() = false

    override suspend fun getServerInfo() = serverInfo
}