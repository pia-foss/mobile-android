package com.kape.regions.vpn.data

import com.kape.data.vpnserver.VpnServer
import com.kape.data.vpnserver.VpnServerInfo
import com.kape.regions.common.data.Clock
import com.kape.regions.vpn.data.bundled.BundledVpnRegionDataSource
import com.kape.regions.vpn.data.local.DedicatedIpDataSource
import com.kape.regions.vpn.data.remote.VpnRegionRemoteDataSource
import com.kape.regions.vpn.domain.VpnRegionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.koin.core.annotation.Singleton

private const val REFRESH_INTERVAL_MS = 30_000L

@Singleton(binds = [VpnRegionRepository::class])
internal class VpnRegionRepositoryImpl(
    private val remote: VpnRegionRemoteDataSource,
    private val bundled: BundledVpnRegionDataSource,
    private val dedicatedIps: DedicatedIpDataSource,
    private val store: VpnRegionStore,
    private val clock: Clock,
) : VpnRegionRepository {
    private val loadMutex = Mutex()
    private val refreshMutex = Mutex()
    private var lastRefreshMillis: Long? = null

    private val regions: Flow<VpnRegions> = store.regions.onStart { loadBundledIfEmpty() }.filterNotNull()

    override fun observeServers(): Flow<List<VpnServer>> =
        combine(regions, store.latencies, dedicatedIps.observeDedicatedIps()) { regions, latencies, dips ->
            regions.servers.withLatencies(latencies).withDedicatedIps(dips)
        }

    override fun observeIsDefaultList(): Flow<Boolean> = regions.map { it.isBundled }.distinctUntilChanged()

    override suspend fun refreshServers(locale: String) =
        refreshMutex.withLock {
            val now = clock.nowMillis()
            val last = lastRefreshMillis
            if (last != null && now - last < REFRESH_INTERVAL_MS) return@withLock

            val fetched = remote.fetchRegions(locale) ?: return@withLock
            lastRefreshMillis = now
            val info = fetched.info.takeIf { it.isValid() } ?: bundled.getRegions().info
            store.regions.value = VpnRegions(fetched.servers, info, isBundled = false)
        }

    override suspend fun refreshLatencies() {
        store.latencies.value = remote.pingRegions().orEmpty()
    }

    override fun clearLatencies() {
        store.latencies.value = null
    }

    override fun hasLatencies(): Boolean = store.latencies.value != null

    override suspend fun getServerInfo(): VpnServerInfo {
        loadBundledIfEmpty()
        return checkNotNull(store.regions.value).info
    }

    private suspend fun loadBundledIfEmpty() {
        if (store.regions.value != null) return
        loadMutex.withLock {
            if (store.regions.value == null) {
                // compareAndSet, so a network list that arrived meanwhile is never replaced.
                store.regions.compareAndSet(null, bundled.getRegions())
            }
        }
    }
}