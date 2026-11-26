package com.kape.regions.vpn.domain.usecase

import com.kape.regions.vpn.domain.VpnRegionRepository
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Factory

@Factory
class ObserveIsDefaultVpnServerListUseCase(
    private val repository: VpnRegionRepository,
) {
    operator fun invoke(): Flow<Boolean> = repository.observeIsDefaultList()
}