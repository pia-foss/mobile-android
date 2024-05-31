package com.kape.obfuscationregionselection.util

import com.kape.utils.shadowsocksserver.ShadowsocksServer

data class ShadowsocksServerItem(val type: ItemType)

sealed class ItemType {
    data object HeadingFavorites : ItemType()
    data object HeadingAll : ItemType()
    data class Content(
        val isFavorite: Boolean,
        val enableFavorite: Boolean = true,
        val server: ShadowsocksServer,
    ) : ItemType()
}