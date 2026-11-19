package com.kape.regions.vpn.data.remote

import com.kape.regions.common.FakeRegionsApi
import com.privateinternetaccess.regions.RegionLowerLatencyInformation
import com.privateinternetaccess.regions.model.VpnRegionsResponse
import com.privateinternetaccess.regions.model.VpnRegionsResponse.Region
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

internal class VpnRegionRemoteDataSourceTest {
    private val api = FakeRegionsApi()
    private val dataSource = RegionsApiVpnRegionRemoteDataSource(api)

    @Test
    fun `fetches the regions for the locale as a non-bundled list`() =
        runTest {
            api.vpnRegions = VpnRegionsResponse(groups = emptyMap(), regions = listOf(region("de")))

            val regions = dataSource.fetchRegions("de-DE")

            assertEquals(listOf("de-DE"), api.requestedLocales)
            assertEquals(listOf("de"), regions!!.servers.map { it.key })
            assertFalse(regions.isBundled)
        }

    @Test
    fun `returns null when the request fails, even with a response`() =
        runTest {
            api.vpnRegions = VpnRegionsResponse(groups = emptyMap(), regions = listOf(region("de")))
            api.error = Error("offline")

            assertNull(dataSource.fetchRegions("en"))
        }

    @Test
    fun `returns null when there is no response`() =
        runTest {
            assertNull(dataSource.fetchRegions("en"))
        }

    @Test
    fun `maps ping results by region`() =
        runTest {
            api.pings = listOf(RegionLowerLatencyInformation("de", 42), RegionLowerLatencyInformation("fr", 7))

            assertEquals(mapOf("de" to 42L, "fr" to 7L), dataSource.pingRegions())
        }

    @Test
    fun `returns no latencies when pinging fails`() =
        runTest {
            api.pings = listOf(RegionLowerLatencyInformation("de", 42))
            api.error = Error("offline")
            assertNull(dataSource.pingRegions())
        }

    private fun region(id: String) =
        Region(
            id = id,
            name = id,
            country = "DE",
            dns = "$id.example",
            geo = false,
            offline = false,
            latitude = null,
            longitude = null,
            autoRegion = true,
            portForward = false,
            proxy = emptyList(),
            servers = emptyMap(),
        )
}