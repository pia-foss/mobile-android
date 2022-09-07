package com.kape.region_selection.data

import app.cash.turbine.test
import com.kape.core.server.Server
import com.kape.region_selection.domain.RegionDataSource
import com.kape.region_selection.utils.adaptServers
import com.privateinternetaccess.regions.RegionLowerLatencyInformation
import com.privateinternetaccess.regions.model.RegionsResponse
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@ExperimentalCoroutinesApi
internal class RegionRepositoryTest {

    private val source: RegionDataSource = mockk()
    private val regionsResponse =
        "{\"groups\":{\"ikev2\":[{\"name\":\"ikev2\",\"ports\":[500,4500]}],\"meta\":[{\"name\":\"meta\",\"ports\":[443,8080]}],\"ovpntcp\":[{\"name\":\"openvpn_tcp\",\"ports\":[80,443,853,8443]}],\"ovpnudp\":[{\"name\":\"openvpn_udp\",\"ports\":[8080,853,123,53]}],\"proxysocks\":[{\"name\":\"socks\",\"ports\":[1080]}],\"proxyss\":[{\"name\":\"shadowsocks\",\"ports\":[443]}],\"wg\":[{\"name\":\"wireguard\",\"ports\":[1337]}]},\"regions\":[{\"id\":\"nigeria\",\"name\":\"Nigeria\", \"id\":\"ng\",\"country\":\"NG\",\"auto_region\":true,\"dns\":\"nigeria.privacy.network\",\"port_forward\":true,\"geo\":true,\"offline\":false,\"servers\":{\"ikev2\":[{\"ip\":\"146.70.65.132\",\"cn\":\"nigeria405\"},{\"ip\":\"146.70.65.146\",\"cn\":\"nigeria406\"}],\"meta\":[{\"ip\":\"146.70.65.130\",\"cn\":\"nigeria405\"},{\"ip\":\"146.70.65.143\",\"cn\":\"nigeria406\"}],\"ovpntcp\":[{\"ip\":\"146.70.65.141\",\"cn\":\"nigeria405\",\"van\":false},{\"ip\":\"146.70.65.156\",\"cn\":\"nigeria406\",\"van\":false}],\"ovpnudp\":[{\"ip\":\"146.70.65.135\",\"cn\":\"nigeria405\",\"van\":false},{\"ip\":\"146.70.65.152\",\"cn\":\"nigeria406\",\"van\":false}],\"wg\":[{\"ip\":\"146.70.65.137\",\"cn\":\"nigeria405\"},{\"ip\":\"146.70.65.147\",\"cn\":\"nigeria406\"}]}}]}"
    private val pingResponse = RegionLowerLatencyInformation("ng", "endpoint", 359)

    private lateinit var repository: RegionRepository

    @BeforeEach
    internal fun setUp() {
        repository = RegionRepository(source)
    }

    @Test
    fun `fetch regions succeeds`() = runTest {
        val response: RegionsResponse = Json.decodeFromString(regionsResponse)
        val map = adaptServers(response)
        val expected = map.values.toList()
        coEvery { source.fetchRegions(any()) } returns flow {
            emit(response)
        }
        repository.fetchRegions("en").test {
            val actual = awaitItem()
            awaitComplete()
            assertEquals(expected, actual)
        }
    }

    @Test
    fun `fetch regions fails`() = runTest {
        val expected = emptyList<Server>()
        coEvery { source.fetchRegions(any()) } returns flow {
            emit(null)
        }
        repository.fetchRegions("en").test {
            val actual = awaitItem()
            awaitComplete()
            assertEquals(expected, actual)
        }
    }

    @Test
    fun `update latencies succeeds`() = runTest {
        val response: RegionsResponse = Json.decodeFromString(regionsResponse)
        coEvery { source.fetchRegions(any()) } returns flow {
            emit(response)
        }
        coEvery { source.pingRequests() } returns flow {
            emit(listOf(pingResponse))
        }
        repository.fetchRegions("").test {
            awaitItem()
            awaitComplete()
        }
        repository.fetchLatencies().test {
            val actual = awaitItem()
            awaitComplete()
            assertFalse(actual.isEmpty())
            assertEquals(pingResponse.latency, actual.first().latency?.toLong())
        }
    }

    @Test
    fun `update latencies fails`() = runTest {
        coEvery { source.fetchRegions(any()) } returns flow {
            emit(null)
        }
        coEvery { source.pingRequests() } returns flow {
            emit(null)
        }
        repository.fetchRegions("en")
        repository.fetchLatencies().test {
            val actual = awaitItem()
            awaitComplete()
            assertTrue(actual.isEmpty())
        }
    }
}