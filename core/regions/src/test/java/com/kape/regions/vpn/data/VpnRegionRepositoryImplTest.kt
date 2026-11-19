package com.kape.regions.vpn.data

import com.kape.data.vpnserver.VpnServer
import com.kape.data.vpnserver.VpnServer.ServerGroup
import com.kape.data.vpnserver.VpnServerInfo
import com.kape.regions.common.data.Clock
import com.kape.regions.vpn.data.bundled.BundledVpnRegionDataSource
import com.kape.regions.vpn.data.local.DedicatedIpDataSource
import com.kape.regions.vpn.data.remote.VpnRegionRemoteDataSource
import com.kape.regions.vpn.dedicatedIp
import com.kape.regions.vpn.domain.VPN_PING_TIMEOUT_MS
import com.kape.regions.vpn.vpnServer
import com.privateinternetaccess.account.model.response.DedicatedIPInformationResponse.DedicatedIPInformation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class VpnRegionRepositoryImplTest {
    private val bundledInfo = VpnServerInfo(listOf("bundled"), listOf(8080), listOf(443))
    private val remote = FakeRemote()
    private val bundled =
        object : BundledVpnRegionDataSource {
            override suspend fun getRegions() = VpnRegions(listOf(vpnServer("bundled")), bundledInfo, isBundled = true)
        }
    private val dips = MutableStateFlow<List<DedicatedIPInformation>>(emptyList())
    private val clock = FakeClock()
    private val repository =
        VpnRegionRepositoryImpl(remote, bundled, DedicatedIpDataSource { dips }, VpnRegionStore(), clock)

    @Test
    fun `starts with the bundled list`() =
        runTest {
            assertEquals(listOf("bundled"), repository.observeServers().first().map { it.key })
            assertTrue(repository.observeIsDefaultList().first())
        }

    @Test
    fun `replaces the bundled list after a successful refresh`() =
        runTest {
            remote.regions = VpnRegions(listOf(vpnServer("network")), VpnServerInfo(), isBundled = false)

            repository.refreshServers("en")

            assertEquals(listOf("network"), repository.observeServers().first().map { it.key })
            assertFalse(repository.observeIsDefaultList().first())
        }

    @Test
    fun `keeps the bundled list when the refresh fails, and retries on the next call`() =
        runTest {
            remote.regions = null
            repository.refreshServers("en")
            assertEquals(listOf("bundled"), repository.observeServers().first().map { it.key })

            remote.regions = VpnRegions(listOf(vpnServer("network")), VpnServerInfo(), isBundled = false)
            repository.refreshServers("en")
            assertEquals(listOf("network"), repository.observeServers().first().map { it.key })
        }

    @Test
    fun `skips refreshes within 30 seconds of the last successful one`() =
        runTest {
            remote.regions = VpnRegions(listOf(vpnServer("network")), VpnServerInfo(), isBundled = false)
            repository.refreshServers("en")
            clock.now += 29_999
            repository.refreshServers("en")
            assertEquals(1, remote.fetches)

            clock.now += 1
            repository.refreshServers("en")
            assertEquals(2, remote.fetches)
        }

    @Test
    fun `uses the bundled server info when the network's is incomplete`() =
        runTest {
            remote.regions = VpnRegions(listOf(vpnServer("network")), VpnServerInfo(), isBundled = false)

            repository.refreshServers("en")

            assertEquals(bundledInfo, repository.getServerInfo())
        }

    @Test
    fun `applies latencies, giving unanswered regions the ping timeout`() =
        runTest {
            remote.regions = VpnRegions(listOf(vpnServer("a"), vpnServer("b")), VpnServerInfo(), isBundled = false)
            remote.latencies = mapOf("a" to 42L)
            repository.refreshServers("en")

            repository.refreshLatencies()

            val latencies = repository.observeServers().first().associate { it.key to it.latency }
            assertEquals(mapOf("a" to "42", "b" to VPN_PING_TIMEOUT_MS.toString()), latencies)
            assertTrue(repository.hasLatencies())
        }

    @Test
    fun `clearing latencies removes them`() =
        runTest {
            remote.latencies = mapOf("bundled" to 42L)
            repository.refreshLatencies()

            repository.clearLatencies()

            assertNull(
                repository
                    .observeServers()
                    .first()
                    .single()
                    .latency,
            )
            assertFalse(repository.hasLatencies())
        }

    @Test
    fun `adds a server for each dedicated IP, reached through that IP`() =
        runTest {
            dips.value = listOf(dedicatedIp(regionKey = "bundled", ip = "203.0.113.7"))

            val servers = repository.observeServers().first()

            val dipServer = servers.single { it.isDedicatedIp }
            assertEquals(2, servers.size)
            assertEquals("203.0.113.7", dipServer.dedicatedIp)
            assertEquals("203.0.113.7:1337", dipServer.endpointFor(ServerGroup.WIREGUARD).ip)
            assertEquals(8080, dipServer.endpointFor(ServerGroup.OPENVPN_UDP).port)
        }

    @Test
    fun `ignores dedicated IPs for unknown regions`() =
        runTest {
            dips.value = listOf(dedicatedIp(regionKey = "nowhere"))

            assertEquals(listOf("bundled"), repository.observeServers().first().map { it.key })
        }
}

private fun VpnServer.endpointFor(group: ServerGroup) = endpoints.getValue(group).single()

private class FakeRemote : VpnRegionRemoteDataSource {
    var regions: VpnRegions? = null
    var latencies: Map<String, Long>? = null
    var fetches = 0

    override suspend fun fetchRegions(locale: String): VpnRegions? {
        fetches++
        return regions
    }

    override suspend fun pingRegions(): Map<String, Long>? = latencies
}

private class FakeClock : Clock {
    var now = 1_000_000L

    override fun nowMillis(): Long = now
}