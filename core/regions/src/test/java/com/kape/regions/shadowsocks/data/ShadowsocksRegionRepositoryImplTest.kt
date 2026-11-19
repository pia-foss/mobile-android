package com.kape.regions.shadowsocks.data

import com.kape.data.shadowsocksserver.ShadowsocksServer
import com.kape.regions.common.data.Clock
import com.kape.regions.common.data.RegionDetails
import com.kape.regions.common.data.RegionDetailsDataSource
import com.kape.regions.shadowsocks.data.bundled.BundledShadowsocksRegionDataSource
import com.kape.regions.shadowsocks.data.local.ShadowsocksRegionLocalDataSource
import com.kape.regions.shadowsocks.data.remote.ShadowsocksRegionRemoteDataSource
import com.kape.regions.shadowsocks.server
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class ShadowsocksRegionRepositoryImplTest {
    private val remote = FakeRemote()
    private val local = FakeLocal()
    private val bundled = FakeBundled()
    private val regionDetails =
        object : RegionDetailsDataSource {
            override suspend fun getRegionDetails() = mapOf("uk_manchester" to RegionDetails(name = "UK Manchester", iso = "GB"))
        }
    private val clock = FakeClock()
    private val repository = ShadowsocksRegionRepositoryImpl(remote, local, bundled, regionDetails, clock)

    @Test
    fun `falls back to the bundled servers, with region names, while none are stored`() =
        runTest {
            bundled.servers = listOf(server(host = "1.1.1.1", region = "uk_manchester"))

            assertEquals(
                listOf(server(host = "1.1.1.1", region = "UK Manchester", iso = "GB")),
                repository.observeServers().first(),
            )
        }

    @Test
    fun `keeps a server unchanged when its region is unknown`() =
        runTest {
            bundled.servers = listOf(server(host = "1.1.1.1", region = "mars_base"))

            assertEquals(bundled.servers, repository.observeServers().first())
        }

    @Test
    fun `stores refreshed servers, with region names, and serves them instead of the bundled ones`() =
        runTest {
            bundled.servers = listOf(server(host = "bundled"))
            remote.servers = listOf(server(host = "1.1.1.1", region = "uk_manchester"))

            repository.refreshServers("en")

            assertEquals(
                listOf(server(host = "1.1.1.1", region = "UK Manchester", iso = "GB")),
                repository.observeServers().first(),
            )
        }

    @Test
    fun `keeps the stored servers when the refresh fails`() =
        runTest {
            val stored = listOf(server(host = "stored"))
            local.servers.value = stored
            remote.servers = emptyList()

            repository.refreshServers("en")

            assertEquals(stored, repository.observeServers().first())
        }

    @Test
    fun `skips refreshes within 30 seconds of the last one`() =
        runTest {
            repository.refreshServers("en")
            clock.now += 29_999
            repository.refreshServers("en")
            assertEquals(1, remote.calls)

            clock.now += 1
            repository.refreshServers("en")
            assertEquals(2, remote.calls)
        }
}

private class FakeRemote : ShadowsocksRegionRemoteDataSource {
    var servers: List<ShadowsocksServer> = emptyList()
    var calls = 0

    override suspend fun fetchServers(locale: String): List<ShadowsocksServer> {
        calls++
        return servers
    }
}

private class FakeLocal : ShadowsocksRegionLocalDataSource {
    val servers = MutableStateFlow<List<ShadowsocksServer>>(emptyList())
    private val selected = MutableStateFlow<ShadowsocksServer?>(null)

    override fun observeServers(): Flow<List<ShadowsocksServer>> = servers

    override suspend fun saveServers(servers: List<ShadowsocksServer>) {
        this.servers.value = servers
    }

    override fun observeSelectedServer(): Flow<ShadowsocksServer?> = selected

    override suspend fun saveSelectedServer(server: ShadowsocksServer) {
        selected.value = server
    }
}

private class FakeBundled : BundledShadowsocksRegionDataSource {
    var servers: List<ShadowsocksServer> = emptyList()

    override suspend fun getServers(): List<ShadowsocksServer> = servers
}

private class FakeClock : Clock {
    var now = 1_000_000L

    override fun nowMillis(): Long = now
}