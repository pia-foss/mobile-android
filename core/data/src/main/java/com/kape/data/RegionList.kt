package com.kape.data

fun <S> arrangeRegions(
    servers: List<S>,
    isFavorite: (S) -> Boolean,
    includeFavoritesInAll: Boolean = true,
    canBeFavorite: (S) -> Boolean = { true },
): List<RegionServerItem<S>> {
    val rows =
        servers.map { server ->
            RegionServerItem(
                RegionItemType.Content(isFavorite = isFavorite(server), enableFavorite = canBeFavorite(server), server = server),
            )
        }
    val favorites = rows.filter { (it.type as RegionItemType.Content).isFavorite }.distinct()
    val all = (if (includeFavoritesInAll) rows else rows - favorites.toSet()).distinct()
    if (favorites.isEmpty()) return all
    return buildList {
        add(RegionServerItem(RegionItemType.HeadingFavorites))
        addAll(favorites)
        add(RegionServerItem(RegionItemType.HeadingAll))
        addAll(all)
    }
}

fun <S> List<RegionServerItem<S>>.searchByName(
    query: String,
    name: (S) -> String,
): List<RegionServerItem<S>> {
    if (query.isEmpty()) return emptyList()
    return filter { (it.type as? RegionItemType.Content)?.server?.let(name)?.contains(query, ignoreCase = true) == true }
        .distinct()
}