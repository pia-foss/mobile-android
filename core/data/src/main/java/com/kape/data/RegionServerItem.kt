package com.kape.data

data class RegionServerItem<S>(
    val type: RegionItemType<S>,
)

sealed class RegionItemType<out S> {
    data object HeadingFavorites : RegionItemType<Nothing>()

    data object HeadingAll : RegionItemType<Nothing>()

    data class Content<S>(
        val isFavorite: Boolean,
        val enableFavorite: Boolean = true,
        val server: S,
    ) : RegionItemType<S>()
}