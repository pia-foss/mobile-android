package com.kape.vpnregionselection.ui.vm

import com.kape.data.RegionItemType
import com.kape.data.RegionServerItem
import com.kape.data.arrangeRegions
import com.kape.data.vpnserver.VpnServer
import com.kape.vpnregionselection.domain.usecase.AUTO_REGION
import com.kape.vpnregionselection.vpnServer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class VpnRegionsStateTest {
    private val de = vpnServer("de")
    private val fr = vpnServer("fr")
    private val state = VpnRegionsState(regions = arrangeRegions(listOf(AUTO_REGION, de, fr), isFavorite = { it == fr }))

    private fun List<RegionServerItem<VpnServer>>.keys() = map { (it.type as RegionItemType.Content).server.key }

    @Test
    fun `the TV all tab starts at the auto region, skipping the favorites section`() {
        assertEquals(listOf("auto", "de", "fr"), state.allRegions.keys())
    }

    @Test
    fun `the TV favorites tab lists favorites once`() {
        assertEquals(listOf("fr"), state.favoriteRegions.keys())
    }

    @Test
    fun `the list shows search results while searching`() {
        assertEquals(state.regions, state.visibleRegions)

        val searching = state.copy(searchResults = emptyList())

        assertEquals(emptyList<RegionServerItem<VpnServer>>(), searching.visibleRegions)
    }
}