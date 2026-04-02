package com.kape.vpnconnect.domain

import com.kape.utils.vpnserver.VpnServer
import com.kape.vpnmanager.data.models.ClientConfiguration

interface ConnectionConfigurationUseCase {
    fun generateConnectionConfiguration(server: VpnServer): ClientConfiguration
    suspend fun updateServerConfig(server: VpnServer): Boolean
}