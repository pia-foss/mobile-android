package com.kape.regionselection.util

import com.kape.utils.server.Server


data class ServerItem(val type: ItemType)

sealed class ItemType {
    data object HeadingFavorites : ItemType()
    data object HeadingAll : ItemType()
    data class Content(
        val isFavorite: Boolean,
        val enableFavorite: Boolean = true,
        val server: Server,
    ) : ItemType()
}