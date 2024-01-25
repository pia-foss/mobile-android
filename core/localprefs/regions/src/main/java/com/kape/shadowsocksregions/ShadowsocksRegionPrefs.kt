package com.kape.shadowsocksregions

import android.content.Context
import com.kape.utils.Prefs
import com.kape.utils.shadowsocksserver.ShadowsocksServer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private const val SHADOWSOCKS_VPN_FAVORITES = "favorites"
private const val SHADOWSOCKS_SELECTED_SERVER = "shadowsocks-selected-server"
private const val SHADOWSOCKS_SERVERS = "shadowsocks-servers"

class ShadowsocksRegionPrefs(context: Context) : Prefs(context, "shadowsocks-regions") {

    fun addToFavorites(shadowsocksServerName: String) {
        val favorites = prefs.getStringSet(SHADOWSOCKS_VPN_FAVORITES, mutableSetOf())
        favorites!!.add(shadowsocksServerName)
        prefs.edit().putStringSet(SHADOWSOCKS_VPN_FAVORITES, favorites).apply()
    }

    fun removeFromFavorites(shadowsocksServerName: String) {
        val favourites = prefs.getStringSet(SHADOWSOCKS_VPN_FAVORITES, mutableSetOf())
        favourites!!.remove(shadowsocksServerName)
        prefs.edit().putStringSet(SHADOWSOCKS_VPN_FAVORITES, favourites).apply()
    }

    fun isFavorite(shadowsocksServerName: String): Boolean =
        prefs.getStringSet(SHADOWSOCKS_VPN_FAVORITES, emptySet())?.contains(shadowsocksServerName) ?: false

    fun setSelectShadowsocksServer(shadowsocksServer: ShadowsocksServer) =
        prefs.edit().putString(SHADOWSOCKS_SELECTED_SERVER, Json.encodeToString(shadowsocksServer)).apply()

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