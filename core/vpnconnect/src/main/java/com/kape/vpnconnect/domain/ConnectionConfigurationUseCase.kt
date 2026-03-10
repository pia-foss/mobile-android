package com.kape.vpnconnect.domain

import com.kape.utils.vpnserver.VpnServer
import com.kape.vpnmanager.data.models.ClientConfiguration
import kotlinx.coroutines.flow.Flow

interface ConnectionConfigurationUseCase {
    fun generateConnectionConfiguration(server: VpnServer): ClientConfiguration
    fun updateServerConfig(server: VpnServer): Flow<Boolean>
}