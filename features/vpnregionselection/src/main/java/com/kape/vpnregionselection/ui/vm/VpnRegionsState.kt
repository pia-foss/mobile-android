package com.kape.vpnregionselection.ui.vm

import com.kape.data.AUTO_KEY
import com.kape.data.RegionItemType
import com.kape.data.RegionServerItem
import com.kape.data.vpnserver.VpnServer

data class VpnRegionsState(
    val regions: List<RegionServerItem<VpnServer>> = emptyList(),
    val searchResults: List<RegionServerItem<VpnServer>>? = null,
    val isRefreshing: Boolean = false,
) {
    val visibleRegions: List<RegionServerItem<VpnServer>> get() = searchResults ?: regions

    val allRegions: List<RegionServerItem<VpnServer>>
        get() = regions.drop(regions.indexOfFirst { it.isAutoRegion() }.coerceAtLeast(0))

    val favoriteRegions: List<RegionServerItem<VpnServer>>
        get() = allRegions.filter { (it.type as? RegionItemType.Content)?.isFavorite == true }
}

internal fun RegionServerItem<VpnServer>.isAutoRegion() = (type as? RegionItemType.Content)?.server?.key == AUTO_KEY