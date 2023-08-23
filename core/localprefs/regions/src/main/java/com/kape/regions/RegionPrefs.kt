package com.kape.regions

import android.content.Context
import com.kape.utils.Prefs

private const val FAVORITES = "favorites"
private const val SELECTED_SERVER = "selected-server"
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
        return prefs.getStringSet(FAVORITES, emptySet())?.contains(serverName) ?: false
    }

    fun getFavoriteServers(): List<String> {
        return prefs.getStringSet(FAVORITES, emptySet())?.toList() ?: emptyList()
    }

    fun selectServer(serverName: String) {
        prefs.edit().putString(SELECTED_SERVER, serverName).apply()
    }

    fun getSelectedServerKey() = prefs.getString(SELECTED_SERVER, "")
}