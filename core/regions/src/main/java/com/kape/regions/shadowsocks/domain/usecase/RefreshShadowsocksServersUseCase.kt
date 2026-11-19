package com.kape.regions.shadowsocks.domain.usecase

import com.kape.regions.shadowsocks.domain.ShadowsocksRegionRepository
import org.koin.core.annotation.Factory

@Factory
class RefreshShadowsocksServersUseCase(
    private val repository: ShadowsocksRegionRepository,
) {
    suspend operator fun invoke(locale: String) = repository.refreshServers(locale)
}