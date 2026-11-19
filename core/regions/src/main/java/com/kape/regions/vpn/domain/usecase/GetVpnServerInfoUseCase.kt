package com.kape.regions.vpn.domain.usecase

import com.kape.data.vpnserver.VpnServerInfo
import com.kape.regions.vpn.domain.VpnRegionRepository
import org.koin.core.annotation.Factory

@Factory
class GetVpnServerInfoUseCase(
    private val repository: VpnRegionRepository,
) {
    suspend operator fun invoke(): VpnServerInfo = repository.getServerInfo()
}