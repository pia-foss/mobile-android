package com.kape.shadowsocksregions

import android.content.Context
import com.kape.regions.data.ServerData
import com.kape.utils.Prefs
import com.kape.utils.shadowsocksserver.ShadowsocksServer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private const val SHADOWSOCKS_VPN_FAVORITES = "favorite-servers"
private const val SHADOWSOCKS_SELECTED_SERVER = "shadowsocks-selected-server"
private const val SHADOWSOCKS_SERVERS = "shadowsocks-servers"

class ShadowsocksRegionPrefs(context: Context) : Prefs(context, "shadowsocks-regions") {

    fun addToFavorites(shadowsocksServerName: String) {
        val favorites = getFavoriteShadowsocksServers().toMutableList()
        favorites.add(ServerData(shadowsocksServerName, false))
        prefs.edit().putString(SHADOWSOCKS_VPN_FAVORITES, Json.encodeToString(favorites)).apply()
    }

    fun removeFromFavorites(shadowsocksServerName: String) {
        val favourites = getFavoriteShadowsocksServers().toMutableList()
        favourites.remove(ServerData(shadowsocksServerName, false))
        prefs.edit().putString(SHADOWSOCKS_VPN_FAVORITES, Json.encodeToString(favourites)).apply()
    }

    fun isFavorite(shadowsocksServerName: String): Boolean {
        val favorites: List<ServerData> = getFavoriteShadowsocksServers()
        return favorites.contains(ServerData(shadowsocksServerName, false))
    }

    private fun getFavoriteShadowsocksServers(): List<ServerData> {
        val list: List<ServerData> = prefs.getString(SHADOWSOCKS_VPN_FAVORITES, null)?.let {
            Json.decodeFromString(it)
        } ?: emptyList()
        return list
    }

    fun setSelectShadowsocksServer(shadowsocksServer: ShadowsocksServer) =
        prefs.edit().putString(SHADOWSOCKS_SELECTED_SERVER, Json.encodeToString(shadowsocksServer))
            .apply()

    fun getSelectedShadowsocksServer(): ShadowsocksServer? =
        prefs.getString(SHADOWSOCKS_SELECTED_SERVER, null)?.let {
            Json.decodeFromString(it)
        }

    fun setShadowsocksServers(shadowsocksServers: List<ShadowsocksServer>) =
        prefs.edit().putString(SHADOWSOCKS_SERVERS, Json.encodeToString(shadowsocksServers)).apply()

    fun getShadowsocksServers(): List<ShadowsocksServer> =
        prefs.getString(SHADOWSOCKS_SERVERS, null)?.let {
            Json.decodeFromString(it)
        } ?: emptyList()
}