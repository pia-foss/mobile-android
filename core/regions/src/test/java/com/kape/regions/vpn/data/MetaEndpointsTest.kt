package com.kape.regions.vpn.data

import com.kape.regions.vpn.vpnServer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.random.Random

internal class MetaEndpointsTest {
    private val random = Random(0)

    @Test
    fun `prefers the selected region, then the lowest latency`() {
        val servers =
            listOf(
                vpnServer("slow", latency = "300"),
                vpnServer("fast", latency = "20"),
                vpnServer("selected", latency = "500"),
            )

        val endpoints = selectMetaEndpoints(servers, selected = servers[2], random)

        assertEquals(listOf("meta-selected", "meta-fast"), endpoints.map { it.endpoint })
    }

    @Test
    fun `fills with random regions while no latencies are known`() {
        val servers = listOf(vpnServer("a"), vpnServer("b"), vpnServer("c"))

        val endpoints = selectMetaEndpoints(servers, selected = null, random)

        assertEquals(2, endpoints.size)
        assertEquals(2, endpoints.map { it.endpoint }.distinct().size)
    }

    @Test
    fun `uses each region once and connects through a pinned certificate`() {
        val only = vpnServer("only", latency = "20")

        val endpoints = selectMetaEndpoints(listOf(only), selected = only, random)

        val endpoint = endpoints.single()
        assertEquals(true, endpoint.isProxy)
        assertEquals(true, endpoint.usePinnedCertificate)
        assertEquals("cn-only", endpoint.certificateCommonName)
    }
}