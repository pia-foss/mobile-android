package com.kape.connection.domain.usecase

import com.kape.connection.model.QuickConnectServer
import com.kape.connection.vpnServer
import com.kape.regions.data.ServerData
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class QuickConnectItemsTest {
    private val servers = listOf("de", "fr", "it", "es", "nl", "pl", "se").map { vpnServer(it) }

    private fun keys(items: List<QuickConnectItem>) = items.map { it.server.key + if (it.isFavorite) "*" else "" }

    @Test
    fun `favorites come first, then recent servers newest first, each once`() {
        val items =
            quickConnectItems(
                recent = listOf(QuickConnectServer("de"), QuickConnectServer("fr"), QuickConnectServer("it")),
                favorites = listOf(ServerData("It", isDip = false)),
                servers = servers,
            )

        assertEquals(listOf("it*", "fr", "de"), keys(items))
    }

    @Test
    fun `at most five servers are offered`() {
        val items = quickConnectItems(recent = servers.map { QuickConnectServer(it.key) }, favorites = emptyList(), servers = servers)

        assertEquals(5, items.size)
    }

    @Test
    fun `servers no longer offered are skipped`() {
        val items =
            quickConnectItems(
                recent = listOf(QuickConnectServer("gone"), QuickConnectServer("de")),
                favorites = emptyList(),
                servers = servers,
            )

        assertEquals(listOf("de"), keys(items))
    }

    @Test
    fun `a dedicated IP is a different server from its region`() {
        val dip = vpnServer("de", dedicatedIp = "1.2.3.4")
        val items =
            quickConnectItems(
                recent = listOf(QuickConnectServer("de"), QuickConnectServer("de", isDip = true)),
                favorites = emptyList(),
                servers = servers + dip,
            )

        assertEquals(listOf(true, false), items.map { it.server.isDedicatedIp })
    }
}