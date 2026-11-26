package com.kape.regions.vpn.data

import com.kape.regions.common.data.RegionDetails
import com.kape.regions.vpn.FakeVpnRegionRepository
import com.kape.regions.vpn.vpnServer
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class VpnRegionDetailsDataSourceTest {
    @Test
    fun `maps the current servers by lower-cased key to their name and country`() =
        runTest {
            val repository = FakeVpnRegionRepository(listOf(vpnServer("UK_Manchester"), vpnServer("de")))

            assertEquals(
                mapOf(
                    "uk_manchester" to RegionDetails(name = "Name UK_Manchester", iso = "XX"),
                    "de" to RegionDetails(name = "Name de", iso = "XX"),
                ),
                VpnRegionDetailsDataSource(repository).getRegionDetails(),
            )
        }
}