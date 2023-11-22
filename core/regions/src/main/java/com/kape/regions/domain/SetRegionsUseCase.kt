package com.kape.regions.domain

import com.kape.regions.RegionPrefs
import com.kape.utils.server.Server

class SetRegionsUseCase(private val prefs: RegionPrefs) {

    fun setVpnServers(servers: List<Server>) = prefs.setServers(servers = servers)
}