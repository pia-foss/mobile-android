package com.kape.regions.vpn.data

import com.kape.data.vpnserver.VpnServer
import com.kape.data.vpnserver.VpnServerInfo
import com.kape.regions.vpn.domain.VPN_PING_TIMEOUT_MS
import com.kape.regions.vpn.vpnServer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class VpnRegionStoreTest {
    private val store = VpnRegionStore()

    @Test
    fun `has no servers before any regions are loaded`() {
        assertTrue(store.currentServers().isEmpty())
    }

    @Test
    fun `clears stale latencies while none have been measured`() {
        store.regions.value = regions(vpnServer("de", latency = "42"))

        assertEquals(listOf<String?>(null), store.currentServers().map { it.latency })
    }

    @Test
    fun `applies measured latencies, and the ping timeout to servers that did not answer`() {
        store.regions.value = regions(vpnServer("de"), vpnServer("fr", latency = "7"))
        store.latencies.value = mapOf("de" to 42L)

        assertEquals(listOf("42", VPN_PING_TIMEOUT_MS.toString()), store.currentServers().map { it.latency })
    }

    @Test
    fun `does not change the stored servers when applying latencies`() {
        val server = vpnServer("de")
        store.regions.value = regions(server)
        store.latencies.value = mapOf("de" to 42L)

        store.currentServers()

        assertNull(
            store.regions.value!!
                .servers
                .single()
                .latency,
        )
    }

    private fun regions(vararg servers: VpnServer) = VpnRegions(servers.toList(), VpnServerInfo(), isBundled = false)
}