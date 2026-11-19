package com.kape.regions.vpn.domain

import com.kape.regions.vpn.vpnServer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.random.Random

internal class OptimalServerSelectionTest {
    private val random = Random(0)

    @Test
    fun `picks the lowest-latency auto region`() {
        val fastest = vpnServer("b", latency = "20")
        val servers = listOf(vpnServer("a", latency = "80"), fastest, vpnServer("c", latency = null))

        assertEquals(fastest, selectOptimalServer(servers, random))
    }

    @Test
    fun `never picks geo-located, offline or non-auto servers`() {
        val eligible = vpnServer("ok", latency = "90")
        val servers =
            listOf(
                vpnServer("geo", latency = "1", isGeo = true),
                vpnServer("off", latency = "1", isOffline = true),
                vpnServer("manual", latency = "1", autoRegion = false),
                eligible,
            )

        assertEquals(eligible, selectOptimalServer(servers, random))
    }

    @Test
    fun `picks an auto region at random while no latencies are known`() {
        val servers = listOf(vpnServer("a"), vpnServer("b"), vpnServer("manual", autoRegion = false))

        val picked = selectOptimalServer(servers, random)

        assertEquals(true, picked.key in setOf("a", "b"))
    }

    @Test
    fun `falls back to the first server when there is no auto region`() {
        val servers = listOf(vpnServer("a", autoRegion = false), vpnServer("b", autoRegion = false))

        assertEquals(servers.first(), selectOptimalServer(servers, random))
    }
}