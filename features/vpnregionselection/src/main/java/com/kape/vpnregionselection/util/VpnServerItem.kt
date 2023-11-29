package com.kape.vpnregionselection.util

import com.kape.utils.server.VpnServer

data class ServerItem(val type: ItemType)

sealed class ItemType {
    data object HeadingFavorites : ItemType()
    data object HeadingAll : ItemType()
    data class Content(
        val isFavorite: Boolean,
        val enableFavorite: Boolean = true,
        val server: VpnServer,
    ) : ItemType()
}