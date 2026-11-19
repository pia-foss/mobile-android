package com.kape.regions.vpn.domain.usecase

import com.kape.data.vpnserver.VpnServer
import com.kape.regions.vpn.domain.VpnRegionRepository
import com.kape.regions.vpn.domain.selectOptimalServer
import kotlinx.coroutines.flow.first
import org.koin.core.annotation.Factory
import kotlin.random.Random

@Factory
class GetOptimalVpnServerUseCase(
    private val repository: VpnRegionRepository,
) {
    suspend operator fun invoke(): VpnServer = selectOptimalServer(repository.observeServers().first { it.isNotEmpty() }, Random.Default)
}