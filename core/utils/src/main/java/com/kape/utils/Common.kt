package com.kape.utils

suspend fun <S, I> arrangeServers(
    items: List<S>?,
    currentItems: List<I>,
    toServer: (I) -> S?,
    isFavorite: suspend (S) -> Boolean,
    toItem: (S, isFavorite: Boolean) -> I,
    headingFavorites: I,
    headingAll: I,
    includeFavoritesInAll: Boolean = true,
    filter: ((S) -> Boolean)? = null,
): List<I> {
    val sourceServers = items ?: currentItems.mapNotNull(toServer)
    val all = mutableListOf<I>()
    val favorites = mutableListOf<I>()

    for (server in sourceServers) {
        if (filter != null && !filter(server)) {
            continue
        }
        val favorite = isFavorite(server)
        val item = toItem(server, favorite)
        if (favorite) {
            favorites.add(item)
            if (includeFavoritesInAll) {
                all.add(item)
            }
        } else {
            all.add(item)
        }
    }

    val arrangedServers = mutableListOf<I>()
    if (favorites.isNotEmpty()) {
        arrangedServers.add(headingFavorites)
        arrangedServers.addAll(favorites.distinct())
        arrangedServers.add(headingAll)
    }
    arrangedServers.addAll(all.distinct())

    return arrangedServers
}

fun <I> filterServersByName(
    items: List<I>,
    value: String,
    name: (I) -> String?,
): List<I> {
    if (value.isEmpty()) {
        return emptyList()
    }
    return items
        .filter { name(it)?.lowercase()?.contains(value.lowercase()) == true }
        .distinct()
}