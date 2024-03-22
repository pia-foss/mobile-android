package com.kape.vpnregions

import android.content.Context
import com.kape.regions.data.ServerData
import com.kape.utils.Prefs
import com.kape.utils.vpnserver.VpnServer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private const val VPN_FAVORITES = "favorites"
private const val VPN_SELECTED_SERVER = "selected-vpn-server"
private const val VPN_SERVERS = "servers"
private const val VPN_RECONNECT = "reconnect"

class VpnRegionPrefs(context: Context) : Prefs(context, "vpn-regions") {

    fun addToFavorites(serverData: ServerData) {
        val favorites = getFavoriteVpnServers().toMutableList()
        favorites.add(serverData)
        prefs.edit().putString(VPN_FAVORITES, Json.encodeToString(favorites)).apply()
    }

    fun removeFromFavorites(serverData: ServerData) {
        val favourites = getFavoriteVpnServers().toMutableList()
        favourites.remove(serverData)
        prefs.edit().putString(VPN_FAVORITES, Json.encodeToString(favourites)).apply()
    }

    fun isFavorite(serverData: ServerData): Boolean {
        val favorites: List<ServerData> = getFavoriteVpnServers()
        return favorites.contains(serverData)
    }

    fun isFavorite(serverName: String, isDip: Boolean): Boolean {
        return isFavorite(ServerData(serverName, isDip))
    }

    fun getFavoriteVpnServers(): List<ServerData> {
        val list: List<ServerData> = prefs.getString(VPN_FAVORITES, null)?.let {
            Json.decodeFromString(it)
        } ?: emptyList()
        return list
    }

    fun selectVpnServer(vpnServer: VpnServer) {
        prefs.edit().putString(VPN_SELECTED_SERVER, Json.encodeToString(vpnServer)).apply()
        setVpnReconnect(true)
    }

    fun getSelectedServer(): VpnServer? = prefs.getString(VPN_SELECTED_SERVER, null)?.let {
        Json.decodeFromString(it)
    }

    fun needsVpnReconnect() = prefs.getBoolean(VPN_RECONNECT, false)

    fun setVpnReconnect(needsReconnect: Boolean) {
        prefs.edit().putBoolean(VPN_RECONNECT, needsReconnect).apply()
    }
}