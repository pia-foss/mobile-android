package com.kape.regions.shadowsocks.domain.usecase

import com.kape.data.shadowsocksserver.ShadowsocksServer
import com.kape.regions.shadowsocks.domain.ShadowsocksRegionRepository
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Factory

@Factory
class ObserveShadowsocksServersUseCase(
    private val repository: ShadowsocksRegionRepository,
) {
    operator fun invoke(): Flow<List<ShadowsocksServer>> = repository.observeServers()
}