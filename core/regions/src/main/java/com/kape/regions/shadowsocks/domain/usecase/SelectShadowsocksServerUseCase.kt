package com.kape.regions.shadowsocks.domain.usecase

import com.kape.data.shadowsocksserver.ShadowsocksServer
import com.kape.regions.shadowsocks.domain.ShadowsocksRegionRepository
import org.koin.core.annotation.Factory

@Factory
class SelectShadowsocksServerUseCase(
    private val repository: ShadowsocksRegionRepository,
) {
    suspend operator fun invoke(server: ShadowsocksServer) = repository.selectServer(server)
}