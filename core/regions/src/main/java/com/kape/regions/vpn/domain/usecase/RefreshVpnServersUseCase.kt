package com.kape.regions.vpn.domain.usecase

import com.kape.regions.vpn.domain.VpnRegionRepository
import org.koin.core.annotation.Factory

@Factory
class RefreshVpnServersUseCase(
    private val repository: VpnRegionRepository,
) {
    suspend operator fun invoke(
        locale: String,
        isConnected: Boolean,
        force: Boolean = false,
    ) {
        if (!force && !isConnected && repository.hasLatencies()) return

        repository.refreshServers(locale)
        if (isConnected) {
            repository.clearLatencies()
        } else {
            repository.refreshLatencies()
        }
    }
}