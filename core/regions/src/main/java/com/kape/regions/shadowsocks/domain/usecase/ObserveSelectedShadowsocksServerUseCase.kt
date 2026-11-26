package com.kape.regions.shadowsocks.domain.usecase

import com.kape.data.shadowsocksserver.ShadowsocksServer
import com.kape.regions.shadowsocks.domain.ShadowsocksRegionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import org.koin.core.annotation.Factory

@Factory
class ObserveSelectedShadowsocksServerUseCase(
    private val repository: ShadowsocksRegionRepository,
) {
    operator fun invoke(): Flow<ShadowsocksServer> =
        combine(repository.observeServers(), repository.observeSelectedServer()) { servers, selected ->
            servers.firstOrNull { it.host == selected?.host }
                ?: servers.firstOrNull()?.also { repository.selectServer(it) }
        }.filterNotNull()
            .distinctUntilChanged()
}