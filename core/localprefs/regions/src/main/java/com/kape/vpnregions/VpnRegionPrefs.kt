package com.kape.vpnregions

import android.content.Context
import com.kape.utils.Prefs
import com.kape.utils.vpnserver.VpnServer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private const val VPN_FAVORITES = "favorites"
private const val VPN_SELECTED_SERVER = "selected-server"
private const val VPN_SERVERS = "servers"

class VpnRegionPrefs(context: Context) : Prefs(context, "vpn-regions") {

    fun addToFavorites(vpnServerName: String) {
        val favorites = getFavoriteVpnServers().toMutableList()
        favorites.add(vpnServerName)
        prefs.edit().putString(VPN_FAVORITES, Json.encodeToString(favorites)).apply()
    }

    fun removeFromFavorites(vpnServerName: String) {
        val favourites = getFavoriteVpnServers().toMutableList()
        favourites.remove(vpnServerName)
        prefs.edit().putString(VPN_FAVORITES, Json.encodeToString(favourites)).apply()
    }

    fun isFavorite(vpnServerName: String): Boolean {
        val favorites: List<String> = getFavoriteVpnServers()
        return favorites.contains(vpnServerName)
    }

    fun getFavoriteVpnServers(): List<String> {
        val list: List<String> = prefs.getString(VPN_FAVORITES, null)?.let {
            Json.decodeFromString(it)
        } ?: emptyList()
        return list
    }

    fun selectVpnServer(vpnServerKey: String) {
        prefs.edit().putString(VPN_SELECTED_SERVER, vpnServerKey).apply()
    }

    fun getSelectedVpnServerKey() = prefs.getString(VPN_SELECTED_SERVER, "")

    fun setVpnServers(vpnServers: List<VpnServer>) =
        prefs.edit().putString(VPN_SERVERS, Json.encodeToString(vpnServers)).apply()

    fun getVpnServers(): List<VpnServer> =
        prefs.getString(VPN_SERVERS, null)?.let {
            Json.decodeFromString(it)
        } ?: emptyList()
}