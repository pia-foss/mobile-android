package com.kape.regions.shadowsocks.data

import com.kape.data.shadowsocksserver.ShadowsocksServer
import com.kape.regions.common.data.Clock
import com.kape.regions.common.data.RegionDetailsDataSource
import com.kape.regions.shadowsocks.data.bundled.BundledShadowsocksRegionDataSource
import com.kape.regions.shadowsocks.data.local.ShadowsocksRegionLocalDataSource
import com.kape.regions.shadowsocks.data.remote.ShadowsocksRegionRemoteDataSource
import com.kape.regions.shadowsocks.domain.ShadowsocksRegionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.koin.core.annotation.Singleton

private const val REFRESH_INTERVAL_MS = 30_000L

@Singleton(binds = [ShadowsocksRegionRepository::class])
internal class ShadowsocksRegionRepositoryImpl(
    private val remote: ShadowsocksRegionRemoteDataSource,
    private val local: ShadowsocksRegionLocalDataSource,
    private val bundled: BundledShadowsocksRegionDataSource,
    private val regionDetails: RegionDetailsDataSource,
    private val clock: Clock,
) : ShadowsocksRegionRepository {
    private val refreshMutex = Mutex()
    private var lastRefreshMillis: Long? = null

    override fun observeServers(): Flow<List<ShadowsocksServer>> =
        local.observeServers().map { stored ->
            stored.ifEmpty { bundled.getServers().withRegionDetails() }
        }

    override suspend fun refreshServers(locale: String) =
        refreshMutex.withLock {
            val now = clock.nowMillis()
            val last = lastRefreshMillis
            if (last != null && now - last < REFRESH_INTERVAL_MS) return@withLock
            lastRefreshMillis = now

            val servers = remote.fetchServers(locale)
            if (servers.isNotEmpty()) {
                local.saveServers(servers.withRegionDetails())
            }
        }

    override fun observeSelectedServer(): Flow<ShadowsocksServer?> = local.observeSelectedServer()

    override suspend fun selectServer(server: ShadowsocksServer) = local.saveSelectedServer(server)

    private suspend fun List<ShadowsocksServer>.withRegionDetails(): List<ShadowsocksServer> {
        val details = regionDetails.getRegionDetails()
        return map { server ->
            details[server.region.lowercase()]
                ?.let { server.copy(region = it.name, iso = it.iso) }
                ?: server
        }
    }
}