package com.kape.region_selection.utils

import android.content.Context
import com.kape.core.Prefs

private const val FAVORITES = "favorites"

class RegionPrefs(context: Context) : Prefs(context, "regions") {

    fun addToFavorites(serverName: String) {
        val favorites = prefs.getStringSet(FAVORITES, mutableSetOf())
        favorites!!.add(serverName)
        prefs.edit().putStringSet(FAVORITES, favorites).apply()
    }

    fun removeFromFavorites(serverName: String) {
        val favourites = prefs.getStringSet(FAVORITES, mutableSetOf())
        favourites!!.remove(serverName)
        prefs.edit().putStringSet(FAVORITES, favourites).apply()
    }

    fun isFavorite(serverName: String): Boolean {
        return prefs.getStringSet(FAVORITES, emptySet())!!.contains(serverName)
    }

}